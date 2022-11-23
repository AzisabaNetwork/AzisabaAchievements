package net.azisaba.azisabaachievements.common.util;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public interface SQLThrowableConsumer<T> {
    void accept(@NotNull T t) throws SQLException;
}
