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

    override fun progressAchievement(uuid: UUID, key: Key, count: Long): CompletableFuture<Boolean> =
        CompletableFuture.failedFuture(
            UnsupportedOperationException("Progress is managed by an external ProgressProvider")
        )

    override fun getChildAchievements(key: Key): CompletableFuture<Set<AchievementData>> {
        return try {
            CompletableFuture.completedFuture(DataProvider.getChildAchievements(queryExecutor, key))
        } catch (t: Throwable) {
            CompletableFuture<Set<AchievementData>>().apply { completeExceptionally(t) }
        }
    }

    override fun deleteAchievementBlocking(key: Key) {
        try {
            queryExecutor.queryVoid("DELETE FROM `achievements` WHERE `key` = ?") { ps ->
                ps.setString(1, key.toString())
                ps.executeUpdate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun deleteAchievementAsync(key: Key) {
        deleteAchievementBlocking(key)
    }
}
