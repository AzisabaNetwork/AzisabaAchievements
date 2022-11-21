plugins {
    java
}

allprojects {
    group = "net.azisaba.azisabaachievements"
    version = "0.0.1-SNAPSHOT"

    apply {
        plugin("java")
    }

    java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}
