package hu.roland;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

public class RecursiveWalkerTest extends TestCase {

    @Test
    public void testCountBlankLines() {
        RecursiveWalker rw = new RecursiveWalker();
        var file = "hello.ex";
        assertEquals(true, Files.exists(new File(file).toPath()));
        assertEquals(8, rw.countBlankLines(file));
    }

    @Test
    public void testCountCommentsLines() {
        RecursiveWalker rw = new RecursiveWalker();
        String javaFileName = "ObjectNsiRepository.java";
        assertEquals(true, Files.exists(new File(javaFileName).toPath()));
        assertEquals(7, rw.countCommentsLines(javaFileName));
    }

    @Test
    public void testLstFromString() {
        String exts = "java,sh";
        assertEquals(Arrays.asList("java","sh"), new RecursiveWalker().lstFromString(exts));
    }

    @Test
    public void testGetExtTest(){
        String filename = "simple.java";
        String hardname = "recursiveWalk-1.0-SNAPSHOT.jar";
        assertEquals(RecursiveWalker.getExt(filename), "java");
        assertEquals(RecursiveWalker.getExt(hardname), "jar");

    }
}