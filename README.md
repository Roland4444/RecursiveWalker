

# RecursiveWalker

required java 17


# Build:
mvn package

# Пример запуска:
java -jar recursiveWalk-1.0-SNAPSHOT-jar-with-dependencies.jar -path='/home/roland/IdeaProjects/RecurseWalker/' --recursive --max-depth=7 --thread=3  --exclude-ext='sh'


java -jar recursiveWalk-1.0-SNAPSHOT-jar-with-dependencies.jar -path='/home/roland/IdeaProjects/RecurseWalker/' --recursive --max-depth=7 --thread=3 --include-ext='java' --exclude-ext='sh'


# Примеры результатов работы 
в папке result


# written by Roman Pastushkov, 2024