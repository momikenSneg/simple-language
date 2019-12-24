# opjava_Searcher

build in /opjava_Searcher:
```
maven clean install
```

run with next jvm options:
```
-Xms400m -Xmx2g -XX:+UseG1GC -XX:+OptimizeFill
```

programm can be run witn the next parameters:
```
-h : help - prints possible arguments
--name <file name> <folder name>: search for a file in the folder
--data '<text>' <folder|file>: search for substring in file or in files in the folder
```
