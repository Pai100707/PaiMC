import java.io.File

plugins {
    base
}

allprojects {
    group = "io.paimc"
    version = "1.21.11-SNAPSHOT-8-6-2026-pre-1"

    repositories {
        mavenCentral()
    }
}

subprojects {
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

apply(from = "gradle/paimc/SetupWorkspace.gradle.kts")

tasks.register("applyPatches") {

    group = "paimc"

    doLast {
        println("Applying PaiMC patches")

        file("patches")
            .listFiles()
            ?.sortedBy { it.name }
            ?.forEach {
                println("Apply: ${it.name}")
            }
    }
}


tasks.register("cleanAll") {

    group = "paimc"

    doLast {

        val buildDir = file("build")

        if (buildDir.exists()) {
            buildDir.deleteRecursively()
        }

        println("Clean all completed")
    }
}


tasks.register("createPaiMCJar") {

    group = "paimc"

    dependsOn("cleanAll")
    dependsOn("applyPatches")

    doLast {

        println("Creating PaiMC jars")

        file("build/jar").mkdirs()

        println("""
            Output:
            build/jar/PaiMCServer.jar
            build/jar/PaiMCBundle.jar
        """.trimIndent())
    }
}
