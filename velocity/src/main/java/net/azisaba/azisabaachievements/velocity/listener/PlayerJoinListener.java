package net.azisaba.azisabaachievements.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.azisaba.azisabaachievements.api.Logger;
import net.azisaba.azisabaachievements.common.sql.DatabaseManager;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class PlayerJoinListener {
    private final DatabaseManager databaseManager;

    public PlayerJoinListener(@NotNull DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Subscribe
    public void onPlayerJoin(LoginEvent e) {
        // update player data
        try {
            databaseManager.queryVoid("INSERT INTO `players` (`id`, `name`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `name` = VALUES(`name`)", ps -> {
                ps.setString(1, e.getPlayer().getUniqueId().toString());
                ps.setString(2, e.getPlayer().getUsername());
                ps.executeUpdate();
            });
        } catch (SQLException ex) {
            Logger.getCurrentLogger().error("Failed to update player data", ex);
        }
    }
}
