plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

sourceSets {
    main {
        java.srcDirs(
            "io",
            "net",
            "org",
            "com"
        )
    }
}

dependencies {
    implementation(project(":pai-api"))
}
