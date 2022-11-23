dependencies {
    api(project(":api"))
    api("redis.clients:jedis:4.3.1")
    api("org.mariadb.jdbc:mariadb-java-client:3.1.0")
    api("it.unimi.dsi:fastutil:8.5.9")
    @Suppress("GradlePackageUpdate") // 5.x requires Java 11+
    api("com.zaxxer:HikariCP:4.0.3")
    api("org.mariadb.jdbc:mariadb-java-client:3.0.6")
}
