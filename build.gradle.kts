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

    java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    }

    tasks {
        test {
            useJUnitPlatform()
        }

        shadowJar {
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
