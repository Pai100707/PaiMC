plugins {
    base
}

allprojects {
    group = "io.paimc"
    version = "1.21.11-SNAPSHOT-8-5-2026-pre-1"

    repositories {
        mavenCentral()
    }
}

subprojects {
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
