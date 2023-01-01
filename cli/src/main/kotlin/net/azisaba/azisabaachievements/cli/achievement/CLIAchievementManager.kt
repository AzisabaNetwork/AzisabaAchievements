package net.azisaba.azisabaachievements.cli.achievement

import net.azisaba.azisabaachievements.api.Key
import net.azisaba.azisabaachievements.api.achievement.AchievementData
import net.azisaba.azisabaachievements.api.achievement.AchievementFlags
import net.azisaba.azisabaachievements.api.achievement.AchievementHideFlags
import net.azisaba.azisabaachievements.api.achievement.AchievementManager
import net.azisaba.azisabaachievements.api.util.MagicConstantBitField
import net.azisaba.azisabaachievements.common.sql.DataProvider
import net.azisaba.azisabaachievements.common.util.QueryExecutor
import java.util.Optional
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.math.min

object CLIAchievementManager : AchievementManager {
    lateinit var queryExecutor: QueryExecutor

    override fun createAchievement(key: Key, count: Long, point: Int): CompletableFuture<AchievementData> =
        queryExecutor.query("INSERT INTO `achievements` (`key`, `count`, `point`) VALUES (?, ?, ?)") { ps ->
            ps.setString(1, key.toString())
            ps.setLong(2, count)
            ps.setInt(3, point)
            ps.executeUpdate()
            ps.generatedKeys.use { generatedKeys ->
                if (generatedKeys.next()) {
                    val id = generatedKeys.getLong(1)
                    CompletableFuture.completedFuture(
                        AchievementData(
                            id,
                            key,
                            count,
                            point,
                            AchievementHideFlags.NEVER,
                            MagicConstantBitField.of(AchievementFlags::class.java, 0)
                        )
                    )
                } else {
                    CompletableFuture<AchievementData>().apply { completeExceptionally(IllegalStateException("Failed to create achievement")) }
                }
            }
        }

    override fun getAchievement(key: Key): CompletableFuture<Optional<AchievementData>> =
        try {
            CompletableFuture.completedFuture(Optional.ofNullable(DataProvider.getAchievementByKey(queryExecutor, key)))
        } catch (t: Throwable) {
            CompletableFuture<Optional<AchievementData>>().apply { completeExceptionally(t) }
        }

    override fun progressAchievement(uuid: UUID, key: Key, count: Long): CompletableFuture<Boolean> {
        if (count == 0L) {
            return CompletableFuture.completedFuture(false)
        }
        return try {
            val data = DataProvider.getAchievementByKey(queryExecutor, key)
                ?: return CompletableFuture<Boolean>().apply { completeExceptionally(IllegalStateException("Achievement does not exist")) }
            if (data.flags.contains(AchievementFlags.CATEGORY) || data.flags.contains(AchievementFlags.UNOBTAINABLE)) {
                return CompletableFuture<Boolean>().apply { completeExceptionally(IllegalStateException("Achievement is unobtainable")) }
            }
            val achievementId = data.id
            val currentCount = queryExecutor.query(
                "SELECT `count` FROM `player_achievements` WHERE `player_id` = ? AND `achievement_id` = ?"
            ) { ps ->
                ps.setString(1, uuid.toString())
                ps.setLong(2, achievementId)
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        return@query rs.getLong("count")
                    } else {
                        return@query 0L
                    }
                }
            }
            queryExecutor.query(
                "INSERT INTO `player_achievements` (`player_id`, `achievement_id`, `count`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `count` = VALUES(`count`)"
            ) { ps ->
                ps.setString(1, uuid.toString())
                ps.setLong(2, achievementId)
                ps.setLong(3, min(data.count, currentCount + count))
                ps.executeUpdate()
                CompletableFuture.completedFuture(currentCount < data.count && currentCount + count >= data.count)
            }
        } catch (t: Throwable) {
            CompletableFuture<Boolean>().apply { completeExceptionally(t) }
        }
    }

    override fun getChildAchievements(key: Key): CompletableFuture<Set<AchievementData>> {
        return try {
            CompletableFuture.completedFuture(DataProvider.getChildAchievements(queryExecutor, key))
        } catch (t: Throwable) {
            CompletableFuture<Set<AchievementData>>().apply { completeExceptionally(t) }
        }
    }
}
