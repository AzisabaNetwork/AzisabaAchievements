package net.azisaba.azisabaachievements.common.sql;

import com.zaxxer.hikari.HikariDataSource;
import net.azisaba.azisabaachievements.common.util.QueryExecutor;
import net.azisaba.azisabaachievements.common.util.SQLThrowableConsumer;
import net.azisaba.azisabaachievements.common.util.SQLThrowableFunction;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager implements QueryExecutor {
    private final @NotNull HikariDataSource dataSource;

    public DatabaseManager(@NotNull HikariDataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
        createTables();
    }

    @SuppressWarnings("SqlNoDataSourceInspection")
    private void createTables() throws SQLException {
        useStatement(statement -> {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `players` (" +
                    "  `id` VARCHAR(36) NOT NULL," + // uuid
                    "  `name` VARCHAR(32) NOT NULL," + // username
                    "  PRIMARY KEY (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `achievements` (" +
                    "  `id` BIGINT NOT NULL AUTO_INCREMENT," + // internal id
                    "  `key` VARCHAR(255) NOT NULL UNIQUE," + // achievement key
                    "  `count` BIGINT NOT NULL," + // achievement count required to actually "achieve"
                    "  `point` INT NOT NULL," + // achievement point
                    "  `hidden` TINYINT(1) NOT NULL DEFAULT 0," + // see AchievementHideFlags
                    "  `flags` INT NOT NULL DEFAULT 0," + // see AchievementFlags
                    "  `parent_id` BIGINT NOT NULL DEFAULT 0," +
                    "  PRIMARY KEY (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `achievement_translations` (" +
                    "  `id` BIGINT NOT NULL," + // achievement id
                    "  `lang` VARCHAR(5) NOT NULL," + // language (Locale#getLanguage())
                    "  `name` VARCHAR(255) NOT NULL," + // achievement name
                    "  `description` VARCHAR(255) NOT NULL," + // achievement description
                    "  PRIMARY KEY (`id`, `lang`)," +
                    "  FOREIGN KEY (`id`) REFERENCES `achievements` (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `player_achievements` (" +
                    "  `player_id` VARCHAR(36) NOT NULL," + // -> players.id
                    "  `achievement_id` BIGINT NOT NULL," + // -> achievements.id
                    "  `count` BIGINT NOT NULL," +
                    "  PRIMARY KEY (`player_id`, `achievement_id`)," +
                    "  FOREIGN KEY (`player_id`) REFERENCES `players` (`id`)," +
                    "  FOREIGN KEY (`achievement_id`) REFERENCES `achievements` (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");
        });
    }

    @NotNull
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Contract(pure = true)
    public <R> R use(@NotNull SQLThrowableFunction<Connection, R> action) throws SQLException {
        try (Connection connection = getConnection()) {
            return action.apply(connection);
        }
    }

    @Contract(pure = true)
    public void use(@NotNull SQLThrowableConsumer<Connection> action) throws SQLException {
        try (Connection connection = getConnection()) {
            action.accept(connection);
        }
    }

    public void queryVoid(@Language("SQL") @NotNull String sql, @NotNull SQLThrowableConsumer<PreparedStatement> action) throws SQLException {
        use(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                action.accept(statement);
            }
        });
    }

    @Contract
    @Override
    public <R> R query(@Language("SQL") @NotNull String sql, @NotNull SQLThrowableFunction<PreparedStatement, R> action) throws SQLException {
        return use(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                return action.apply(statement);
            }
        });
    }

    @Contract(pure = true)
    public void useStatement(@NotNull SQLThrowableConsumer<Statement> action) throws SQLException {
        use(connection -> {
            try (Statement statement = connection.createStatement()) {
                action.accept(statement);
            }
        });
    }

    /**
     * Closes the data source.
     */
    public void close() {
        dataSource.close();
    }
}
