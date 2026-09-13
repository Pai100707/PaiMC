PaiMC fork from [Paper](https://papermc.io/software/paper/)
------
PaiMC for high-performance Minecraft server and vanilla-like experience

How To Server Admins
------
Paperclip is a jar file that you can build or run just like a normal jar file.

Download Paper from our [downloads page](https://paimc.io/downloads/paimc).
Build Paper using
```bash
./gradlew clean
./gradlew applyPatches
./gradlew createMojmapPaperclipJar
```
and your file is in `paper-server/build/libs/`
Run the Paperclip jar directly from your server. Just like old times

* Documentation on using PaiMC: [docs.paimc.io](https://docs.paimc.io)

How To Plugin Developers
------
* See our API [here](paper-api)
* See upcoming, pending, and recently added API [here](https://github.com/orgs/PaperMC/projects/2/views/4)
* Paper API javadocs here: [papermc.io/javadocs](https://papermc.io/javadocs/)
#### Repository (for paper-api)
##### Maven

```xml
<repository>
    <id>papermc</id>
    <url>https://repo.papermc.io/repository/maven-public/</url>
</repository>
```

```xml
<dependency>
    <groupId>io.papermc.paper</groupId>
    <artifactId>paper-api</artifactId>
    <version>1.21.11-R0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```
##### Gradle
```kotlin
repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
```

