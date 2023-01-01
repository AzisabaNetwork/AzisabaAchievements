import org.gradle.configurationcache.extensions.capitalized

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
        maven { url = uri("https://repo.azisaba.net/repository/maven-public/") }
        maven { url = uri("https://repo.acrylicstyle.xyz/repository/maven-public/") }
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
                        project.findProperty("deploySnapshotURL") ?: System.getProperty("deploySnapshotURL", "https://repo.azisaba.net/repository/maven-snapshots/")
                    else
                        project.findProperty("deployReleasesURL") ?: System.getProperty("deployReleasesURL", "https://repo.azisaba.net/repository/maven-releases/")
                )
            }
        }

        publications {
            create<MavenPublication>("mavenJava${project.name.capitalized()}") {
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

        javadoc {
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
            relocate("xyz.acrylicstyle.util", "net.azisaba.azisabaachievements.libs.xyz.acrylicstyle.util")
            relocate("org.yaml", "net.azisaba.azisabaachievements.libs.org.yaml")
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
