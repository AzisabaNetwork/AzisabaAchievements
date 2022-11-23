plugins {
    java
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

allprojects {
    group = "net.azisaba.azisabaachievements"
    version = "0.0.1-SNAPSHOT"

    apply {
        plugin("java")
        plugin("java-library")
        plugin("maven-publish")
        plugin("com.github.johnrengelman.shadow")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
        withJavadocJar()
        withSourcesJar()
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    }

    val javaComponent = components["java"] as AdhocComponentWithVariants
    javaComponent.withVariantsFromConfiguration(configurations["sourcesElements"]) {
        skip()
    }

    publishing {
        repositories {
            maven {
                name = "repo"
                credentials(PasswordCredentials::class)
                url = uri(
                    if (project.version.toString().endsWith("SNAPSHOT"))
                        project.findProperty("deploySnapshotURL") ?: System.getProperty("deploySnapshotURL", "")
                    else
                        project.findProperty("deployReleasesURL") ?: System.getProperty("deployReleasesURL", "")
                )
            }
        }

        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifact(tasks.getByName("sourcesJar"))
            }
        }
    }

    tasks {
        processResources {
            from(sourceSets.main.get().resources.srcDirs) {
                include("**")
                val tokenReplacementMap = mapOf(
                    "version" to project.version,
                )
                filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
            }
            filteringCharset = "UTF-8"
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            from(projectDir) { include("LICENSE") }
        }

        compileJava {
            options.encoding = "UTF-8"
        }

        test {
            useJUnitPlatform()
        }

        shadowJar {
            relocate("redis.clients", "net.azisaba.azisabaachievements.libs.redis.clients")
            relocate("org.mariadb", "net.azisaba.azisabaachievements.libs.org.mariadb")
            relocate("it.unimi.dsi", "net.azisaba.azisabaachievements.libs.it.unimi.dsi")
            relocate("org.mariadb.jdbc", "net.azisaba.azisabaachievements.libs.org.mariadb.jdbc")
            relocate("com.zaxxer.hikari", "net.azisaba.azisabaachievements.libs.com.zaxxer.hikari")
            archiveClassifier.set("")
        }
    }
}

subprojects {
    tasks {
        shadowJar {
            archiveBaseName.set("${parent!!.name}-${project.name}")
        }
    }
}
