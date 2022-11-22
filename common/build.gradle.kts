dependencies {
    api(project(":api"))
    api("redis.clients:jedis:4.3.1")
    api("org.mariadb.jdbc:mariadb-java-client:3.1.0")
    api("it.unimi.dsi:fastutil:8.5.9")
}

tasks {
    shadowJar {
        relocate("redis.clients", "net.azisaba.azisabaachievements.libs.redis.clients")
        relocate("org.mariadb", "net.azisaba.azisabaachievements.libs.org.mariadb")
        relocate("it.unimi.dsi", "net.azisaba.azisabaachievements.libs.it.unimi.dsi")
    }
}
