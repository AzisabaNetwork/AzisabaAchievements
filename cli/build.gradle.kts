plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

dependencies {
    api(kotlin("stdlib"))
    api(project(":common"))
    api("com.zaxxer:HikariCP:5.0.1")
    api("org.mariadb.jdbc:mariadb-java-client:3.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.slf4j:slf4j-nop:2.0.5")
}
