# Gradle Notes
### For my own reference and learning

- [Gradle 8.10 Javadocs](https://docs.gradle.org/8.10/javadoc/)

## Environment variables
```
variable            : Example                                                   : Description
------------------- : -----------------                                         : -----------
DIRNAME             : C:\runelite-plugin-devel\Wildy_Agility_Loot_Tracker       : 
APP_BASE_NAME       : gradlew                                                   : 
APP_HOME            : C:\runelite-plugin-devel\Wildy_Agility_Loot_Tracker       : 
DEFAULT_JVM_OPTS    : -Xmx64m -Xms64m                                           : 
GRADLE_OPTS         :                                                           : 
JAVA_HOME           : C:\Program Files\Eclipse Adoptium\jdk-11.0.31.11-hotspot  : 
JAVA_EXE            : %JAVA_HOME%\bin\java.exe                                  : 
JAVA_OPTS           :                                                           : 
CLASSPATH           : %APP_HOME%\gradle\wrapper\gradle-wrapper.jar              : 
```

## Other Random Things
```
./gradlew.bat                           : windows batch wrapper script
./gradle/wrapper/gradle-wrapper.jar     : JAR / classpath / the main library/binary
org.gradle.wrapper.GradleWrapperMain    : Main Class
org.gradle.appname                      : Property set by JAVA CLI Macro  (defaults to: gradlew)
```

## Gradle Bat Usage Examples
```Batchfile
.\gradlew.bat clean --info
.\gradlew.bat javadoc --info
.\gradlew.bat build --info
.\gradlew.bat runTest --info
.\gradlew.bat runMain --info
.\gradlew.bat shadowJar --info
```


