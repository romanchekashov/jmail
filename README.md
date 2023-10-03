# JMail - JavaFX desktop app example build with Maven

## Build & Run
### Run app
```shell
mvn clean javafx:run
```

### Create executable jar and run it with script
- [Maven Shade JavaFX runtime components are missing](https://stackoverflow.com/questions/52653836/maven-shade-javafx-runtime-components-are-missing)
```shell
mvn clean install
# chmod +x run-executable-jar.sh
./run-executable-jar.sh
```

### Create executables: Win / Linux / Mac
- [JavaFX, JLink and JPackage](https://dev.to/cherrychain/javafx-jlink-and-jpackage-h9)
- [Tree FX](https://gitlab.com/lucaguada/treefx/-/tree/openjdk16-jpackage-win)
- [javafx-maven-plugin](https://github.com/openjfx/javafx-maven-plugin)
- [jpackage-maven-plugin](https://github.com/petr-panteleyev/jpackage-maven-plugin)
```shell
mvn clean compile javafx:jlink jpackage:jpackage
```

## Resources
### Initial resources
- [YouTube: Building and Deploying Java Client Desktop Applications with JDK 17 and Beyond](https://www.youtube.com/watch?v=jb7m9dL1iSI)
- [src code: jmail javafx app](https://cr.openjdk.org/~prr/javaone/2022/)

#### Create JavaFX sample app:
- [Run HelloWorld using Maven](https://openjfx.io/openjfx-docs/maven)
- [javafx-maven-archetypes](https://github.com/openjfx/javafx-maven-archetypes)
```shell
mvn archetype:generate \                                                 
        -DarchetypeGroupId=org.openjfx \
        -DarchetypeArtifactId=javafx-archetype-simple \
        -DarchetypeVersion=0.0.6 \
        -DgroupId=org.example \
        -DartifactId=sample \
        -Dversion=1.0.0 \
        -Djavafx-version=21
```

- [Download JavaFX](https://gluonhq.com/products/javafx/)
- [Maven JavaFX](https://central.sonatype.com/artifact/org.openjfx/javafx)
- [OpenJFX Docs Samples](https://github.com/openjfx/samples/tree/master)
- [intellij idea : how to debug a java:fx maven project?](https://stackoverflow.com/questions/61340702/intellij-idea-how-to-debug-a-javafx-maven-project)
