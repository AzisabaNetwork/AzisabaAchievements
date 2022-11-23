package net.azisaba.azisabaachievements.common.util;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface QueryExecutor {
    void queryVoid(@Language("SQL") @NotNull String sql, @NotNull SQLThrowableConsumer<PreparedStatement> action) throws SQLException;

    @Contract
    <R> R query(@Language("SQL") @NotNull String sql, @NotNull SQLThrowableFunction<PreparedStatement, R> action) throws SQLException;
}
