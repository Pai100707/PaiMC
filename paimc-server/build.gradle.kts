plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {

    implementation(project(":pai-api"))
}


sourceSets {

    main {

        java.srcDirs(
            "net",
            "io",
            "org",
            "com"
        )
    }
}
