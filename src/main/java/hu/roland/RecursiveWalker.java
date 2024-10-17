package hu.roland;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

public class RecursiveWalker {

    private static final String PATH = "-path";
    private static final String RECURSIVE = "--recursive";
    private static final String MAX_DEPTH = "--max-depth";
    private static final String THREADS= "--thread";
    private static final String INCLUDE_EXT= "--include-ext";
    private static final String EXCLUDE_EXT= "--exclude-ext";

    private String path;
    private boolean recursive = false;
    private int maxDepth = 1;
    private int threads = 1;
    private List<String> includeExt = new ArrayList<>();
    private List <String> excludeExt = new ArrayList<>();
    private Map<String, String> argsMap;
    private Map<String, ExtInfo> infoMap = new HashMap<>();


    private void processArgs(String[] args){
        argsMap = new HashMap<>();
        for (String arg: args) {
            if (arg.contains("=")) {
                String[] parts = arg.split("=");
                argsMap.put(parts[0], parts[1]);
                continue;
            }
            if (arg.equals(RECURSIVE))
                recursive=true;
        }
        if (recursive)
            if (!isNull(argsMap.get(MAX_DEPTH)))
               maxDepth = Integer.parseInt(argsMap.get(MAX_DEPTH));
        if (!isNull(argsMap.get(EXCLUDE_EXT)))
            excludeExt = lstFromString(argsMap.get(EXCLUDE_EXT));
        if (!isNull(argsMap.get(INCLUDE_EXT)))
            includeExt = lstFromString(argsMap.get(INCLUDE_EXT));
        if (!isNull(argsMap.get(THREADS)))
            threads = Integer.parseInt(argsMap.get(THREADS));
        if (!isNull(argsMap.get(PATH)))
            path = argsMap.get(PATH);
    }

    Predicate<String> includePredicate = new Predicate<String>() {
        @Override
        public boolean test(String s) {
            if (includeExt.isEmpty())
                return true;
            return includeExt.stream().anyMatch(a-> s.endsWith(a));
        }
    };

    Predicate<String> excludePredicate = new Predicate<String>() {
        @Override
        public boolean test(String s) {
            if (excludeExt.isEmpty())
                return true;
            return !excludeExt.stream().anyMatch(a-> s.endsWith(a));
        }
    };

    void processFile(File a) {
        var ext = getExt(a.getName());
        var extInfo = infoMap.get(ext);
        if (isNull(extInfo)){
            extInfo = new ExtInfo(ext);
            infoMap.put(ext, extInfo);
        };
        extInfo.addCountFiles();
        extInfo.addSizeFiles(a.length());
        FileInfo fi = countBlankLines(a.getAbsolutePath());
        extInfo.addLinesCount(fi.total);
        extInfo.addEmptyLines(fi.specific);
        extInfo.addCommentsLines(countCommentsLines(a.getAbsolutePath()));
    }

    public List<String> lstFromString(String input){
        return Arrays.asList(input.split("\\s*,\\s*"));
    }

    public RecursiveWalker(String[] args) throws IOException, InterruptedException {
        processArgs(args);
        start();
    }

    public RecursiveWalker(){

    }

    public RecursiveWalker(String path, boolean recursive, int maxDepth, int threads,
                           List<String> includeExt, List <String> excludeExt) throws IOException, InterruptedException {
        this.path = path;
        this.recursive = recursive;
        this.maxDepth = maxDepth;
        this.threads = threads;
        this.includeExt = includeExt;
        this.excludeExt = excludeExt;
        start();
    }

    private void start() throws IOException, InterruptedException {
        var stream2 = generateStream();
        ForkJoinPool pool = new ForkJoinPool(threads);
        pool.submit(() ->
                stream2.parallel()
                .filter(c -> c.toFile().isFile() &&
                        includePredicate.test(c.getFileName().toString()) &&
                        excludePredicate.test(c.getFileName().toString()))
                .forEach(a-> {
                    processFile(a.toFile());
                })
        ).join();
        pool.shutdown();

        infoMap.forEach((a, b) -> {
                System.out.println("##########################################");
                System.out.println("EXTENSION:" + a);
                System.out.println();
                System.out.println("Количество файлов:                  " + b.getCountFiles());
                System.out.println("Размер в байтах:                    " + b.getSizeFiles());
                System.out.println("Количество строк всего:             " + b.getLinesCount());
                System.out.println("Количество не пустых строк:         " + (b.getLinesCount() - b.getEmptyLines()));
                System.out.println("Количество строк с комментариями:   " + (b.getCommentsLines()));
                System.out.println("##########################################\n\n\n");
        });
        }

    private Stream<Path> generateStream() throws IOException {
        return Files.walk(Paths.get(path), maxDepth);
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        long startTime = System.nanoTime();
//        new ResursiveWalker("/home/roland/IdeaProjects/RecurseWalker/", true, 7, 3,
//            Arrays.asList(""), Arrays.asList("java"));

        new RecursiveWalker(args);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("time execution:: " + duration / 1000000000);

    }


    public FileInfo countCondition(String fileName, Predicate<String> guidedPredicate){
        int counter = 0;
        int totalLines = 0;
        try (BufferedReader b = new BufferedReader(new FileReader(fileName))){
            String readLine;
            while ((readLine = b.readLine()) != null) {
                ++totalLines;
                if (guidedPredicate.test(readLine))
                    counter++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new FileInfo(totalLines, counter);
    };

    public FileInfo countBlankLines(String fileName){
        return countCondition(fileName, s -> s.length()==0);
    };

    public int countCommentsLines(String fileName){
        if (fileName.endsWith(".java"))
            return countCondition(fileName, s -> s.trim().startsWith("//")).specific;
        if (fileName.endsWith(".sh"))
            return countCondition(fileName, s -> s.trim().startsWith("#")).specific;
        return 0;
    };

    public static String getExt(String fileName){
        var indexOf = fileName.lastIndexOf(".");
        if (indexOf<0)
            return null;
        return fileName.substring(indexOf+1);
    };

    class ExtInfo{
        private final String ext;

        public ExtInfo(String ext){
            this.ext = ext;
        }

        private AtomicInteger countFiles = new AtomicInteger(0); ///1. Количество файлов
        private AtomicLong sizeFiles     = new AtomicLong(0);    ///2. Размер в байтах
        private AtomicLong linesCount    = new AtomicLong(0);    ///3. Количество строк всего
        private AtomicLong emptyLines    = new AtomicLong(0);    ///Число пустых строк
        private AtomicLong commentsLines = new AtomicLong(0);    ///5. Количество строк с комментариями (учитывать только однострочные комментарий в начале строки, реализовать как минимум для Java кода и Bash скриптов)

        public int getCountFiles(){
            return countFiles.get();
        }

        public void addCountFiles(){ countFiles.incrementAndGet();};

        public long getSizeFiles(){ return sizeFiles.get();}

        public void addSizeFiles(long size){sizeFiles.addAndGet(size); };

        public long getLinesCount(){ return linesCount.get();}

        public void addLinesCount(int size){ linesCount.addAndGet(size);};

        public long getEmptyLines(){ return emptyLines.get();}

        public void addEmptyLines(int size){emptyLines.addAndGet(size);};

        public long getCommentsLines(){return commentsLines.get();}

        public void addCommentsLines(int size){
            commentsLines.addAndGet(size);
        };
    }

    record FileInfo(int total, int specific){};
}


