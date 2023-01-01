repositories {
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

dependencies {
    api(project(":common"))
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
    api("com.zaxxer:HikariCP:5.0.1")
    api("org.mariadb.jdbc:mariadb-java-client:3.1.0")
}

tasks {
    register("processSources", Sync::class.java) {
        from(sourceSets.main.get().allJava.srcDirs) {
            include("**")

            val tokenReplacementMap = mapOf(
                "YOU_SHOULD_NOT_SEE_THIS_AS_VERSION" to parent!!.version,
            )

            filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
        }
        into("$buildDir/src")
    }

    withType<JavaCompile> {
        source = named<Sync>("processSources").get().outputs.files.asFileTree
    }
}
