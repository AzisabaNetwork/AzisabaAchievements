package net.azisaba.azisabaachievements.spigot.listener;

import net.azisaba.azisabaachievements.api.AzisabaAchievementsProvider;
import net.azisaba.azisabaachievements.api.achievement.AchievementTranslationData;
import net.azisaba.azisabaachievements.spigot.data.AchievementDataCache;
import net.azisaba.azisabaachievements.spigot.data.TranslatedAchievement;
import net.azisaba.azisabaachievements.spigot.event.AchievementUnlockedEvent;
import net.azisaba.azisabaachievements.spigot.plugin.SpigotPlugin;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class AchievementUnlockedListener implements Listener {
    private final SpigotPlugin plugin;

    public AchievementUnlockedListener(@NotNull SpigotPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAchievementUnlocked(@NotNull AchievementUnlockedEvent e) {
        AzisabaAchievementsProvider.get()
                .getScheduler()
                .builder(() -> {
                    Player player = Bukkit.getPlayer(e.getUniqueId());
                    if (player == null) {
                        return;
                    }
                    AchievementDataCache cache = plugin.getAchievementDataCache();
                    TranslatedAchievement achievement = cache.getAchievement(e.getAchievement().getKey());
                    AchievementTranslationData translation = achievement.getTranslationForLocale(player.getLocale());
                    String translatedName = translation == null ? e.getAchievement().getKey().toString() : translation.getName();
                    String translatedDescription = translation == null ? e.getAchievement().getKey().toString() : translation.getDescription();
                    translatedName = ChatColor.translateAlternateColorCodes('&', translatedName);
                    translatedDescription = ChatColor.translateAlternateColorCodes('&', translatedDescription);

                    // send message
                    // TODO: hardcoded message
                    TextComponent component = new TextComponent();
                    component.setColor(ChatColor.YELLOW.asBungee());
                    TextComponent prefix = new TextComponent("実績解除！ 「");
                    prefix.setColor(ChatColor.GREEN.asBungee());
                    BaseComponent[] nameComponent = TextComponent.fromLegacyText(translatedName);
                    TextComponent suffix = new TextComponent("」");
                    suffix.setColor(ChatColor.GREEN.asBungee());
                    TextComponent nameWrapper = new TextComponent();
                    for (BaseComponent c : nameComponent) nameWrapper.addExtra(c);
                    BaseComponent[] hover = TextComponent.fromLegacyText(translatedDescription);
                    for (BaseComponent c : hover) {
                        if (c.getColorRaw() == null) {
                            c.setColor(ChatColor.GREEN.asBungee());
                        }
                    }
                    nameWrapper.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                    component.addExtra(prefix);
                    component.addExtra(nameWrapper);
                    component.addExtra(suffix);
                    player.spigot().sendMessage(component);
                    //player.sendMessage(ChatColor.GREEN + "実績解除！ 「" + ChatColor.YELLOW + translatedName + ChatColor.GREEN + "」");

                    // play firework effect
                    Firework spawnedFirework = player.getWorld().spawn(player.getLocation(), Firework.class, firework -> {
                        FireworkMeta meta = firework.getFireworkMeta();
                        meta.addEffect(FireworkEffect.builder()
                                .withColor(Color.GREEN)
                                .withFade(Color.RED)
                                .with(FireworkEffect.Type.BALL)
                                .build());
                        meta.setPower(0);
                        firework.setFireworkMeta(meta);
                    });

                    // detonate after 500ms (equivalent to 10 ticks or 0.5 seconds)
                    AzisabaAchievementsProvider.get()
                            .getScheduler()
                            .builder(spawnedFirework::detonate)
                            .delay(500, TimeUnit.MILLISECONDS)
                            .sync()
                            .schedule();
                }).sync().schedule();
    }
}
