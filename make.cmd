@echo off

javac src\*.java -d .

copy src\*.png .

jar cfm out.jar META-INF\MANIFEST.MF *.class *.png

del *.class

del *.png

