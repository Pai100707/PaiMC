tasks.register("setupWorkspace") {

    group = "paimc"

    doLast {

        val server = file("paimc-server")

        val force = project.hasProperty("force")


        if (server.exists()) {

            if (!force) {

                error(
                    """
                    paimc-server already exists.
                    Refusing to overwrite.

                    Use:
                    ./gradlew setupWorkspace -Pforce
                    """.trimIndent()
                )
            }


            println("Force setup enabled")

            val config = file(
                "paimc-server/build.gradle.kts"
            )


            val tempConfig =
                file("build/cache/paimc-server-build.gradle.kts")


            if (config.exists()) {

                tempConfig.parentFile.mkdirs()

                config.copyTo(
                    tempConfig,
                    overwrite = true
                )
            }


            listOf(
                "net",
                "io",
                "org",
                "com"
            ).forEach {

                file("paimc-server/$it")
                    .deleteRecursively()
            }


            if (tempConfig.exists()) {

                tempConfig.copyTo(
                    config,
                    overwrite = true
                )
            }

        }


        println("Creating PaiMC workspace")


        // next:
        // download
        // remap
        // decompile


        println("Workspace ready")
    }
}
