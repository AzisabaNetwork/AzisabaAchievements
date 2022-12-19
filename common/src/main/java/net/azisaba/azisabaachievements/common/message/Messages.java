package net.azisaba.azisabaachievements.common.message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Messages {
    private static final Yaml YAML = new Yaml();
    private static final Map<String, MessageInstance> LOCALES = new ConcurrentHashMap<>();
    private static MessageInstance fallback;

    public static void load() throws IOException {
        fallback = Optional.ofNullable(load(Locale.ENGLISH.getLanguage())).orElse(MessageInstance.FALLBACK);
        for (String language : Locale.getISOLanguages()) {
            MessageInstance instance = Messages.load(language);
            if (instance != null) {
                LOCALES.put(language, instance);
            } else {
                LOCALES.put(language, fallback);
            }
        }
    }

    @Nullable
    public static MessageInstance load(@NotNull String language) throws IOException {
        try (InputStream in = Messages.class.getResourceAsStream("/messages_" + language + ".yml")) {
            if (in == null) {
                return null;
            }
            Map<Object, Object> map = YAML.load(in);
            return MessageInstance.createSimple(s -> String.valueOf(map.get(s)));
        }
    }

    @NotNull
    public static MessageInstance getInstance(@Nullable Locale locale) {
        Objects.requireNonNull(fallback, "messages not loaded yet");
        if (locale == null) {
            return fallback;
        }
        return LOCALES.getOrDefault(locale.getLanguage(), fallback);
    }
}
