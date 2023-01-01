package net.azisaba.azisabaachievements.spigot.util;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PlayerUtil {
    /**
     * Returns the {@link Chat}.
     * @return the {@link Chat}
     * @throws NoClassDefFoundError If Vault is not installed.
     */
    public static @NotNull Optional<Chat> getChat() throws NoClassDefFoundError {
        RegisteredServiceProvider<Chat> provider = Bukkit.getServicesManager().getRegistration(Chat.class);
        if (provider == null) {
            return Optional.empty();
        }
        return Optional.of(provider.getProvider());
    }

    /**
     * Returns the prefix + display name + suffix.
     * @return the full display name
     */
    @Contract(pure = true)
    public static @NotNull String getFullDisplayName(@NotNull Player player) {
        Optional<String> displayName = Optional.ofNullable(player.getDisplayName());
        try {
            return getChat()
                    .map(chat -> chat.getPlayerPrefix(player) + player.getDisplayName() + chat.getPlayerSuffix(player))
                    .orElse(displayName.orElse(player.getName()));
        } catch (NoClassDefFoundError e) {
            return displayName.orElse(player.getName());
        }
    }
}
