package net.azisaba.azisabaachievements.velocity.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
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
import net.azisaba.azisabaachievements.velocity.reward.RewardService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;

public class AchievementsCommand extends AbstractCommand {
    private final QueryExecutor queryExecutor;
    private final RewardService rewardService;

    public AchievementsCommand(@NotNull QueryExecutor queryExecutor, @NotNull RewardService rewardService) {
        this.queryExecutor = queryExecutor;
        this.rewardService = rewardService;
    }

    @Override
    protected @NotNull LiteralArgumentBuilder<CommandSource> createBuilder() {
        return literal('v' + "achievements")
                .requires(source -> source.hasPermission("azisabaachievements.command.vachievements"))
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
                                .then(literal("reward")
                                        .then(literal("list")
                                                .executes(ctx -> listRewards(ctx.getSource(), ExtraArgType.getKey(ctx, "key")))
                                        )
                                        .then(literal("add")
                                                .then(argument("targets", StringArgumentType.word())
                                                        .then(argument("command", StringArgumentType.greedyString())
                                                                .executes(ctx -> addReward(
                                                                        ctx.getSource(),
                                                                        ExtraArgType.getKey(ctx, "key"),
                                                                        getString(ctx, "targets"),
                                                                        getString(ctx, "command")))
                                                        )
                                                )
                                        )
                                        .then(literal("remove")
                                                .then(argument("reward-id", LongArgumentType.longArg(1))
                                                        .executes(ctx -> removeReward(
                                                                ctx.getSource(),
                                                                LongArgumentType.getLong(ctx, "reward-id")))
                                                )
                                        )
                                        .then(literal("set-enabled")
                                                .then(argument("reward-id", LongArgumentType.longArg(1))
                                                        .then(argument("enabled", BoolArgumentType.bool())
                                                                .executes(ctx -> setRewardEnabled(
                                                                        ctx.getSource(),
                                                                        LongArgumentType.getLong(ctx, "reward-id"),
                                                                        BoolArgumentType.getBool(ctx, "enabled")))
                                                        )
                                                )
                                        )
                                        .then(literal("set-targets")
                                                .then(argument("reward-id", LongArgumentType.longArg(1))
                                                        .then(argument("targets", StringArgumentType.word())
                                                                .executes(ctx -> setRewardTargets(
                                                                        ctx.getSource(),
                                                                        LongArgumentType.getLong(ctx, "reward-id"),
                                                                        getString(ctx, "targets")))
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(literal("reward")
                        .then(literal("delivery")
                                .then(literal("list")
                                        .then(argument("player", StringArgumentType.word())
                                                .executes(ctx -> listDeliveries(
                                                        ctx.getSource(),
                                                        getString(ctx, "player")))
                                        )
                                )
                                .then(literal("requeue")
                                        .then(argument("delivery-id", LongArgumentType.longArg(1))
                                                .executes(ctx -> requeueDelivery(
                                                        ctx.getSource(),
                                                        LongArgumentType.getLong(ctx, "delivery-id")))
                                        )
                                )
                        )
                );
    }

    private int listDeliveries(@NotNull CommandSource source, @NotNull String playerName) {
        try {
            java.util.List<String> deliveries = rewardService.listDeliveries(playerName);
            if (deliveries.isEmpty()) {
                source.sendMessage(Component.text("No reward deliveries found for " + playerName, NamedTextColor.YELLOW));
            } else {
                deliveries.forEach(row -> source.sendMessage(Component.text(row, NamedTextColor.GREEN)));
            }
            return 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int requeueDelivery(@NotNull CommandSource source, long deliveryId) {
        try {
            rewardService.requeue(deliveryId);
            source.sendMessage(Component.text("Requeued reward delivery #" + deliveryId, NamedTextColor.GREEN));
            return 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int listRewards(@NotNull CommandSource source, @NotNull Key key) {
        try {
            java.util.List<String> rewards = rewardService.listRewards(key);
            if (rewards.isEmpty()) {
                source.sendMessage(Component.text("No rewards configured for " + key, NamedTextColor.YELLOW));
            } else {
                rewards.forEach(row -> source.sendMessage(Component.text(row, NamedTextColor.GREEN)));
            }
            return 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int addReward(
            @NotNull CommandSource source,
            @NotNull Key key,
            @NotNull String targets,
            @NotNull String command
    ) {
        try {
            long rewardId = rewardService.addReward(
                    key,
                    new RewardService.CollectionInput(RewardService.parseTargets(targets), command));
            source.sendMessage(Component.text("Added reward #" + rewardId + " to " + key, NamedTextColor.GREEN));
            return 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int removeReward(@NotNull CommandSource source, long rewardId) {
        try {
            rewardService.removeReward(rewardId);
            source.sendMessage(Component.text("Removed reward #" + rewardId, NamedTextColor.GREEN));
            return 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int setRewardEnabled(@NotNull CommandSource source, long rewardId, boolean enabled) {
        try {
            rewardService.setEnabled(rewardId, enabled);
            source.sendMessage(Component.text(
                    "Set reward #" + rewardId + " enabled=" + enabled,
                    NamedTextColor.GREEN));
            return 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int setRewardTargets(
            @NotNull CommandSource source,
            long rewardId,
            @NotNull String targets
    ) {
        try {
            rewardService.setTargets(rewardId, RewardService.parseTargets(targets));
            source.sendMessage(Component.text("Updated targets for reward #" + rewardId, NamedTextColor.GREEN));
            return 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
