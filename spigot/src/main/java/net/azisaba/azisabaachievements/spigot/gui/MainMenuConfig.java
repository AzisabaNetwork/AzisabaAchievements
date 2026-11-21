package net.azisaba.azisabaachievements.spigot.gui;

import net.azisaba.azisabaachievements.spigot.message.SMessages;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Layout of {@link AchievementsMainScreen}, loaded from gui.yml.
 */
public final class MainMenuConfig {
    private static volatile MainMenuConfig instance;

    public final int rows;
    public final @Nullable Material filler;
    public final int playerHeadSlot;
    public final @NotNull List<Button> buttons;

    private MainMenuConfig(int rows, @Nullable Material filler, int playerHeadSlot, @NotNull List<Button> buttons) {
        this.rows = rows;
        this.filler = filler;
        this.playerHeadSlot = playerHeadSlot;
        this.buttons = Collections.unmodifiableList(buttons);
    }

    public static @NotNull MainMenuConfig get() {
        MainMenuConfig config = instance;
        if (config == null) {
            throw new IllegalStateException("MainMenuConfig is not loaded");
        }
        return config;
    }

    public static void set(@NotNull MainMenuConfig config) {
        instance = config;
    }

    public @Nullable Button getButton(int slot) {
        for (Button button : buttons) {
            if (button.slot == slot) {
                return button;
            }
        }
        return null;
    }

    public static @NotNull MainMenuConfig load(@NotNull ConfigurationSection root, @NotNull Logger logger) {
        ConfigurationSection section = root.getConfigurationSection("main-menu");
        if (section == null) {
            throw new IllegalArgumentException("gui.yml: main-menu section is missing");
        }
        int rows = section.getInt("rows", 3);
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("gui.yml: main-menu.rows must be between 1 and 6: " + rows);
        }
        int size = rows * 9;
        Material filler = parseMaterial(section.getString("filler", "BLACK_STAINED_GLASS_PANE"), "main-menu.filler");
        if (filler == Material.AIR) {
            filler = null;
        }
        int playerHeadSlot = section.getInt("player-head-slot", 22);
        if (playerHeadSlot >= size) {
            throw new IllegalArgumentException("gui.yml: main-menu.player-head-slot is out of range: " + playerHeadSlot);
        }
        List<Button> buttons = new ArrayList<>();
        ConfigurationSection buttonsSection = section.getConfigurationSection("buttons");
        if (buttonsSection != null) {
            for (String id : buttonsSection.getKeys(false)) {
                ConfigurationSection b = buttonsSection.getConfigurationSection(id);
                if (b == null) {
                    continue;
                }
                String path = "main-menu.buttons." + id;
                if (!b.contains("slot")) {
                    throw new IllegalArgumentException("gui.yml: " + path + ".slot is missing");
                }
                int slot = b.getInt("slot");
                if (slot < 0 || slot >= size) {
                    throw new IllegalArgumentException("gui.yml: " + path + ".slot is out of range: " + slot);
                }
                if (slot == playerHeadSlot) {
                    throw new IllegalArgumentException("gui.yml: " + path + ".slot conflicts with player-head-slot: " + slot);
                }
                for (Button other : buttons) {
                    if (other.slot == slot) {
                        throw new IllegalArgumentException("gui.yml: " + path + ".slot conflicts with " + other.id + ": " + slot);
                    }
                }
                Material item = parseMaterial(b.getString("item"), path + ".item");
                ButtonType type;
                try {
                    type = ButtonType.valueOf(b.getString("type", "category").toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("gui.yml: " + path + ".type is invalid: " + b.getString("type"));
                }
                String category = b.getString("category", id);
                buttons.add(new Button(id, slot, item, type, category, readLocalized(b, "name"), readLocalized(b, "lore")));
            }
        }
        if (buttons.isEmpty()) {
            logger.warning("gui.yml: main-menu.buttons is empty");
        }
        return new MainMenuConfig(rows, filler, playerHeadSlot, buttons);
    }

    private static @NotNull Material parseMaterial(@Nullable String name, @NotNull String path) {
        if (name == null) {
            throw new IllegalArgumentException("gui.yml: " + path + " is missing");
        }
        Material material = Material.matchMaterial(name);
        if (material == null) {
            throw new IllegalArgumentException("gui.yml: " + path + " is not a valid item: " + name);
        }
        return material;
    }

    /**
     * Reads either a plain string, a list of strings, or a map of locale -> string (or list).
     * Returns null when the key is absent.
     */
    private static @Nullable Map<String, String> readLocalized(@NotNull ConfigurationSection section, @NotNull String key) {
        if (!section.contains(key)) {
            return null;
        }
        Map<String, String> map = new LinkedHashMap<>();
        if (section.isConfigurationSection(key)) {
            ConfigurationSection localized = section.getConfigurationSection(key);
            assert localized != null;
            for (String locale : localized.getKeys(false)) {
                String value = readText(localized, locale);
                if (value != null) {
                    map.put(locale.toLowerCase(Locale.ROOT), value);
                }
            }
        } else {
            String value = readText(section, key);
            if (value != null) {
                map.put("", value);
            }
        }
        return map;
    }

    private static @Nullable String readText(@NotNull ConfigurationSection section, @NotNull String key) {
        if (section.isList(key)) {
            return String.join("\n", section.getStringList(key));
        }
        return section.getString(key);
    }

    public enum ButtonType {
        CATEGORY,
        SEASONAL,
        LEGACY,
    }

    public static final class Button {
        public final @NotNull String id;
        public final int slot;
        public final @NotNull Material item;
        public final @NotNull ButtonType type;
        public final @NotNull String category;
        private final @Nullable Map<String, String> name;
        private final @Nullable Map<String, String> lore;

        private Button(@NotNull String id, int slot, @NotNull Material item, @NotNull ButtonType type, @NotNull String category,
                       @Nullable Map<String, String> name, @Nullable Map<String, String> lore) {
            this.id = id;
            this.slot = slot;
            this.item = item;
            this.type = type;
            this.category = category;
            this.name = name == null ? null : new HashMap<>(name);
            this.lore = lore == null ? null : new HashMap<>(lore);
        }

        public @NotNull String getName(@NotNull Player player) {
            String raw = resolve(name, player);
            if (raw == null) {
                return SMessages.getFormattedMessage(player, "gui.achievementMainScreen." + id + ".title");
            }
            return ChatColor.translateAlternateColorCodes('&', raw);
        }

        public @Nullable List<String> getLore(@NotNull Player player) {
            String raw = resolve(lore, player);
            if (raw == null || raw.isEmpty()) {
                return null;
            }
            String formatted = ChatColor.translateAlternateColorCodes('&', raw);
            List<String> lines = new ArrayList<>();
            Collections.addAll(lines, formatted.split("\n"));
            return lines;
        }

        private static @Nullable String resolve(@Nullable Map<String, String> map, @NotNull Player player) {
            if (map == null || map.isEmpty()) {
                return null;
            }
            String locale = player.getLocale().replaceAll("_.*", "").toLowerCase(Locale.ROOT);
            if (map.containsKey(locale)) return map.get(locale);
            if (map.containsKey("")) return map.get("");
            if (map.containsKey("en")) return map.get("en");
            return map.values().iterator().next();
        }
    }
}
