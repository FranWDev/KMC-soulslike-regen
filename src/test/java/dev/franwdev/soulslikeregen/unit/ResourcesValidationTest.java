package dev.franwdev.soulslikeregen.unit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Validates the mod's resource files (pack.mcmeta and lang JSON files).
 * Ensures all Component.translatable keys used in Java files exist in translation files.
 */
public class ResourcesValidationTest {

    private final Gson gson = new Gson();

    @Test
    void testPackMcmetaExistsAndIsValid() {
        File file = new File("src/main/resources/pack.mcmeta");
        assertTrue(file.exists(), "pack.mcmeta is missing from src/main/resources/");

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            assertNotNull(root, "pack.mcmeta could not be parsed as a JSON object");
            assertTrue(root.has("pack"), "pack.mcmeta must contain a 'pack' object");

            JsonObject packObj = root.getAsJsonObject("pack");
            assertTrue(packObj.has("pack_format"), "pack.mcmeta's 'pack' must contain 'pack_format'");
            assertTrue(packObj.has("description"), "pack.mcmeta's 'pack' must contain 'description'");

            int packFormat = packObj.get("pack_format").getAsInt();
            assertEquals(15, packFormat, "pack_format for Minecraft 1.20.1 must be 15");
        } catch (Exception e) {
            fail("Failed to read/parse pack.mcmeta: " + e.getMessage());
        }
    }

    @Test
    void testLangFilesExistAndAreValidJson() {
        String[] langFiles = {"en_us.json", "es_es.json"};
        for (String lang : langFiles) {
            File file = new File("src/main/resources/assets/soulslikeregen/lang/" + lang);
            assertTrue(file.exists(), lang + " is missing from src/main/resources/assets/soulslikeregen/lang/");

            try (FileReader reader = new FileReader(file)) {
                JsonObject root = gson.fromJson(reader, JsonObject.class);
                assertNotNull(root, lang + " could not be parsed as a JSON object");
                
                // Assert that essential keys are present
                assertTrue(root.has("msg.soulslikeregen.actionbar.normal"), lang + " is missing essential key 'msg.soulslikeregen.actionbar.normal'");
                assertTrue(root.has("msg.soulslikeregen.actionbar.level"), lang + " is missing essential key 'msg.soulslikeregen.actionbar.level'");
                assertTrue(root.has("msg.soulslikeregen.actionbar.exhausted"), lang + " is missing essential key 'msg.soulslikeregen.actionbar.exhausted'");
            } catch (Exception e) {
                fail("Failed to parse " + lang + ": " + e.getMessage());
            }
        }
    }

    @Test
    void testJavaTranslationKeysExistInLangFiles() {
        Set<String> keysInJava = new HashSet<>();
        File sourceDir = new File("src/main/java");
        assertTrue(sourceDir.exists(), "Source directory src/main/java does not exist");

        // Scan java files
        scanJavaFiles(sourceDir, keysInJava);

        assertFalse(keysInJava.isEmpty(), "No translatable keys were found in Java files (did regex fail?)");

        // Load translation files
        String[] langFiles = {"en_us.json", "es_es.json"};
        for (String langName : langFiles) {
            File langFile = new File("src/main/resources/assets/soulslikeregen/lang/" + langName);
            assertTrue(langFile.exists(), langName + " does not exist");

            try (FileReader reader = new FileReader(langFile)) {
                JsonObject root = gson.fromJson(reader, JsonObject.class);
                assertNotNull(root, "Could not parse " + langName);

                Set<String> missingKeys = new HashSet<>();
                for (String key : keysInJava) {
                    if (!root.has(key)) {
                        missingKeys.add(key);
                    }
                }

                assertTrue(missingKeys.isEmpty(), "The following keys used in Java code are missing from " + langName + ": " + missingKeys);
            } catch (Exception e) {
                fail("Failed to process " + langName + ": " + e.getMessage());
            }
        }
    }

    private void scanJavaFiles(File file, Set<String> keys) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    scanJavaFiles(child, keys);
                }
            }
        } else if (file.getName().endsWith(".java")) {
            try {
                String content = Files.readString(file.toPath());
                // Find all string literals starting with msg.soulslikeregen
                Pattern pattern = Pattern.compile("\"(msg\\.soulslikeregen\\.[^\"]+)\"");
                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    keys.add(matcher.group(1));
                }
            } catch (Exception e) {
                fail("Failed to read java file " + file.getAbsolutePath() + ": " + e.getMessage());
            }
        }
    }
}
