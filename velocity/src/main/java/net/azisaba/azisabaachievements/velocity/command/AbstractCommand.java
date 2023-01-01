package net.azisaba.azisabaachievements.velocity.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementFlags;
import net.azisaba.azisabaachievements.common.sql.DataProvider;
import net.azisaba.azisabaachievements.common.util.QueryExecutor;
import net.azisaba.azisabaachievements.velocity.message.VMessages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractCommand {
    @NotNull
    protected abstract LiteralArgumentBuilder<CommandSource> createBuilder();

    @Contract(" -> new")
    @NotNull
    public final BrigadierCommand createCommand() {
        return new BrigadierCommand(createBuilder());
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull LiteralArgumentBuilder<CommandSource> literal(@NotNull String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static <T> @NotNull RequiredArgumentBuilder<CommandSource, T> argument(@NotNull String name, @NotNull ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    @NotNull
    public static <S> String getString(@NotNull CommandContext<S> context, @NotNull String name) {
        return StringArgumentType.getString(context, name);
    }

    @Contract(pure = true)
    public static @NotNull SuggestionProvider<CommandSource> suggestPlayers(@NotNull ProxyServer server) {
        return (source, builder) -> suggest(server.getAllPlayers().stream().map(Player::getUsername), builder);
    }

    @Contract(pure = true)
    public static @NotNull SuggestionProvider<CommandSource> suggestServers(@NotNull ProxyServer server) {
        return (source, builder) ->
                suggest(server.getAllServers()
                                .stream()
                                .map(RegisteredServer::getServerInfo)
                                .map(ServerInfo::getName), builder);
    }

    private static long achievementsLastUpdated = 0;
    private static Set<AchievementData> achievements = null;

    private static Set<AchievementData> getAchievements(@NotNull QueryExecutor queryExecutor) {
        if (achievements == null || System.currentTimeMillis() - achievementsLastUpdated > 1000 * 60 * 5) {
            achievements = DataProvider.getAllAchievements(queryExecutor);
            achievementsLastUpdated = System.currentTimeMillis();
        }
        return achievements;
    }

    @Contract(pure = true)
    public static @NotNull SuggestionProvider<CommandSource> suggestAllAchievementKey(@NotNull QueryExecutor queryExecutor) {
        return (source, builder) -> suggest(getAchievements(queryExecutor).stream().map(AchievementData::getKey).map(String::valueOf).map(s -> "\"" + s + "\""), builder);
    }

    @Contract(pure = true)
    public static @NotNull SuggestionProvider<CommandSource> suggestNonCategoryAchievementKey(@NotNull QueryExecutor queryExecutor) {
        return (source, builder) -> suggest(
                getAchievements(queryExecutor)
                        .stream()
                        .filter(e -> !e.getFlags().contains(AchievementFlags.CATEGORY))
                        .map(e -> e.getKey().toString())
                        .map(s -> "\"" + s + "\""), builder);
    }

    @Contract(pure = true)
    public static @NotNull SuggestionProvider<CommandSource> suggestCategoryKey(@NotNull QueryExecutor queryExecutor) {
        return (source, builder) -> suggest(
                getAchievements(queryExecutor)
                        .stream()
                        .filter(e -> e.getFlags().contains(AchievementFlags.CATEGORY))
                        .map(e -> e.getKey().toString())
                        .map(s -> "\"" + s + "\""), builder);
    }

    @NotNull
    public static CompletableFuture<Suggestions> suggest(@NotNull Stream<String> suggestions, @NotNull SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase(Locale.ROOT);
        suggestions.filter((suggestion) -> matchesSubStr(input, suggestion.toLowerCase(Locale.ROOT))).forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static boolean matchesSubStr(@NotNull String input, @NotNull String suggestion) {
        for(int i = 0; !suggestion.startsWith(input, i); ++i) {
            i = suggestion.indexOf('_', i);
            if (i < 0) {
                return false;
            }
        }
        return true;
    }

    public static int sendMessageMissingPlayer(@NotNull CommandSource source, @Nullable String playerName) {
        VMessages.sendFormatted(source, "general.player-not-found", playerName);
        return 0;
    }

    public static int sendMessageMissingAchievement(@NotNull CommandSource source, @NotNull Key key) {
        VMessages.sendFormatted(source, "general.achievement-not-found", key.toString());
        return 0;
    }

    public static int sendMessageMissingServer(@NotNull CommandSource source, @Nullable String serverName) {
        source.sendMessage(Component.text("Server not found: " + serverName).color(NamedTextColor.RED));
        return 0;
    }
}
