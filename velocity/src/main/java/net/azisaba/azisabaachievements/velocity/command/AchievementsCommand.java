package net.azisaba.azisabaachievements.velocity.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.Key;
import net.azisaba.azisabaachievements.api.achievement.AchievementData;
import net.azisaba.azisabaachievements.api.achievement.AchievementFlags;
import net.azisaba.azisabaachievements.api.achievement.AchievementHideFlags;
import net.azisaba.azisabaachievements.api.util.MagicConstantBitField;
import net.azisaba.azisabaachievements.common.sql.DataProvider;
import net.azisaba.azisabaachievements.common.util.QueryExecutor;
import net.azisaba.azisabaachievements.velocity.achievement.VelocityAchievementManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;

public class AchievementsCommand extends AbstractCommand {
    private final QueryExecutor queryExecutor;

    public AchievementsCommand(@NotNull QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

    @Override
    protected @NotNull LiteralArgumentBuilder<CommandSource> createBuilder() {
        return literal('v' + "achievements")
                .then(literal("achievement")
                        .then(argument("key", StringArgumentType.string())
                                .suggests(suggestAllAchievementKey(queryExecutor))
                                .then(literal("delete")
                                        .executes(ctx -> deleteAchievement(ctx.getSource(), ExtraArgType.getKey(ctx, "key")))
                                )
                                .then(literal("set")
                                        .then(literal("hidden")
                                                .then(argument("flag", StringArgumentType.word())
                                                        .suggests((source, builder) -> suggest(Arrays.stream(AchievementHideFlags.values()).map(Enum::name).map(String::toLowerCase), builder))
                                                        .executes(ctx -> setHidden(ctx.getSource(), ExtraArgType.getKey(ctx, "key"), ExtraArgType.getEnum(ctx, "flag", AchievementHideFlags.class)))
                                                )
                                        )
                                )
                                .then(literal("flags")
                                        .then(literal("info")
                                                .executes(ctx -> showFlagsInfo(ctx.getSource(), ExtraArgType.getKey(ctx, "key")))
                                        )
                                        .then(literal("add")
                                                .then(argument("flag", StringArgumentType.word())
                                                        .suggests((source, builder) -> suggest(MagicConstantBitField.getNames(AchievementFlags.class).stream().map(String::toLowerCase), builder))
                                                        .executes(ctx -> addFlag(ctx.getSource(), ExtraArgType.getKey(ctx, "key"), ExtraArgType.getMagicConstant(ctx, "flag", AchievementFlags.class)))
                                                )
                                        )
                                        .then(literal("remove")
                                                .then(argument("flag", StringArgumentType.word())
                                                        .suggests((source, builder) -> suggest(MagicConstantBitField.getNames(AchievementFlags.class).stream().map(String::toLowerCase), builder))
                                                        .executes(ctx -> removeFlag(ctx.getSource(), ExtraArgType.getKey(ctx, "key"), ExtraArgType.getMagicConstant(ctx, "flag", AchievementFlags.class)))
                                                )
                                        )
                                )
                        )
                );
    }

    private int deleteAchievement(@NotNull CommandSource source, @NotNull Key key) {
        AzisabaAchievementsProvider.get().getAchievementManager().deleteAchievementBlocking(key);
        VelocityAchievementManager.sendServerData(queryExecutor);
        source.sendMessage(Component.text("Deleted achievement: " + key, NamedTextColor.GREEN));
        return 0;
    }

    private int setHidden(@NotNull CommandSource source, @NotNull Key key, @NotNull AchievementHideFlags flag) {
        if (DataProvider.getAchievementByKey(queryExecutor, key) == null) {
            return sendMessageMissingAchievement(source, key);
        }
        try {
            queryExecutor.queryVoid("UPDATE `achievements` SET `hidden` = ? WHERE `key` = ?", ps -> {
                ps.setByte(1, (byte) flag.ordinal());
                ps.setString(2, key.toString());
                ps.executeUpdate();
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        VelocityAchievementManager.sendServerData(queryExecutor);
        source.sendMessage(Component.text("Set hide flag of " + key + " to " + flag.name(), NamedTextColor.GREEN));
        return 0;
    }

    private int showFlagsInfo(@NotNull CommandSource source, @NotNull Key key) {
        AchievementData achievementData = DataProvider.getAchievementByKey(queryExecutor, key);
        if (achievementData == null) {
            return sendMessageMissingAchievement(source, key);
        }
        source.sendMessage(Component.text(key + " has following flags: ")
                .append(Component.text(String.join(", ", achievementData.getFlags()).toLowerCase(Locale.ROOT),  NamedTextColor.GREEN)));
        return 0;
    }

    private int removeFlag(@NotNull CommandSource source, @NotNull Key key, @NotNull MagicConstantBitField<AchievementFlags> flag) {
        AchievementData achievementData = DataProvider.getAchievementByKey(queryExecutor, key);
        if (achievementData == null) {
            return sendMessageMissingAchievement(source, key);
        }
        try {
            queryExecutor.queryVoid("UPDATE `achievements` SET `flags` = ? WHERE `key` = ?", ps -> {
                ps.setInt(1, achievementData.getFlags().andNotI(flag));
                ps.setString(2, key.toString());
                ps.executeUpdate();
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        VelocityAchievementManager.sendServerData(queryExecutor);
        source.sendMessage(Component.text("Removed flag " + flag.getNames() + " from " + key, NamedTextColor.GREEN));
        return 0;
    }

    private int addFlag(@NotNull CommandSource source, @NotNull Key key, @NotNull MagicConstantBitField<AchievementFlags> flag) {
        AchievementData achievementData = DataProvider.getAchievementByKey(queryExecutor, key);
        if (achievementData == null) {
            return sendMessageMissingAchievement(source, key);
        }
        try {
            queryExecutor.queryVoid("UPDATE `achievements` SET `flags` = ? WHERE `key` = ?", ps -> {
                ps.setInt(1, achievementData.getFlags().orI(flag));
                ps.setString(2, key.toString());
                ps.executeUpdate();
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        VelocityAchievementManager.sendServerData(queryExecutor);
        source.sendMessage(Component.text("Added flag " + flag.getNames() + " to " + key, NamedTextColor.GREEN));
        return 0;
    }
}
