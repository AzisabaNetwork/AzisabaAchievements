package net.azisaba.azisabaachievements.spigot.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class FutureUtil {
    public static <T> @NotNull CompletableFuture<T> exceptionallyCompletedFuture(@NotNull Throwable ex) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(ex);
        return future;
    }
}
