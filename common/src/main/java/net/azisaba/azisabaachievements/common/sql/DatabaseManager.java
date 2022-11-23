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

    private void createTables() throws SQLException {
        useStatement(statement -> {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `players` (" +
                    "  `id` VARCHAR(36) NOT NULL," +
                    "  `name` VARCHAR(36) NOT NULL," +
                    "  `selected_guild` BIGINT NOT NULL DEFAULT -1," +
                    "  `focused_guild` BIGINT NOT NULL DEFAULT -1," + // able to chat to guild without command
                    "  `accepting_invites` TINYINT(1) NOT NULL DEFAULT 1," +
                    "  `translate_kana` TINYINT(1) NOT NULL DEFAULT 1," +
                    "  PRIMARY KEY (`id`)" +
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

    @Contract(pure = true)
    @Override
    public void query(@Language("SQL") @NotNull String sql, @NotNull SQLThrowableConsumer<PreparedStatement> action) throws SQLException {
        use(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                action.accept(statement);
            }
        });
    }

    @Contract(pure = true)
    public <R> R getPrepareStatement(@Language("SQL") @NotNull String sql, @NotNull SQLThrowableFunction<PreparedStatement, R> action) throws SQLException {
        return use(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
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
