dependencies {
    api(project(":api"))
    api("redis.clients:jedis:4.3.1")
    api("it.unimi.dsi:fastutil:8.5.9")
    @Suppress("GradlePackageUpdate") // 5.x requires Java 11+
    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.1.0")
}
