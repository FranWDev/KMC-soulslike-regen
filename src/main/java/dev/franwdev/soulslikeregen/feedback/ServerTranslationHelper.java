package dev.franwdev.soulslikeregen.feedback;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.franwdev.soulslikeregen.SoulslikeRegen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ServerTranslationHelper {

    private static final Map<String, Map<String, String>> TRANSLATIONS = new HashMap<>();
    private static final Gson GSON = new Gson();

    static {
        loadLanguage("en_us");
        loadLanguage("es_es");
    }

    private static void loadLanguage(String lang) {
        String path = "/assets/" + SoulslikeRegen.MODID + "/lang/" + lang + ".json";
        try (InputStream stream = ServerTranslationHelper.class.getResourceAsStream(path)) {
            if (stream != null) {
                JsonObject json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
                Map<String, String> langMap = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    langMap.put(entry.getKey(), entry.getValue().getAsString());
                }
                TRANSLATIONS.put(lang, langMap);
            }
        } catch (Exception e) {
            // Silence or debug log
        }
    }

    public static String getTranslation(String key, String locale) {
        String lang = locale == null ? "en_us" : locale.toLowerCase();
        // Handle cases like "es_ES" by matching the base name or mapping it
        if (lang.contains("_")) {
            String[] parts = lang.split("_");
            lang = parts[0] + "_" + parts[1]; // Ensure normalized e.g., "es_es"
        }
        
        Map<String, String> langMap = TRANSLATIONS.get(lang);
        if (langMap == null || !langMap.containsKey(key)) {
            // Fallback to en_us
            langMap = TRANSLATIONS.get("en_us");
        }
        if (langMap != null && langMap.containsKey(key)) {
            return langMap.get(key);
        }
        return key;
    }

    public static MutableComponent getComponent(ServerPlayer player, String key, Object... args) {
        String locale = player != null ? player.getLanguage() : "en_us";
        String pattern = getTranslation(key, locale);

        try {
            if (args != null && args.length > 0) {
                Object[] formattedArgs = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Component comp) {
                        formattedArgs[i] = comp.getString();
                    } else {
                        formattedArgs[i] = args[i];
                    }
                }
                return Component.literal(String.format(pattern, formattedArgs));
            }
            return Component.literal(pattern);
        } catch (Exception e) {
            return Component.literal(pattern);
        }
    }
}
