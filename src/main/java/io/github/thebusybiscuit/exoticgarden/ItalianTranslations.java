package io.github.thebusybiscuit.exoticgarden;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/** Applies Italian item names from editable YAML files in the plugin data folder. */
final class ItalianTranslations {

    private static final List<String> FILES = Arrays.asList(
        "drinks.yml", "food.yml", "magical_crops.yml", "misc.yml", "plants_and_fruits.yml"
    );

    private ItalianTranslations() {}

    static void apply(@Nonnull JavaPlugin plugin) {
        Map<String, Translation> translations = new HashMap<>();

        for (String file : FILES) {
            load(plugin, file, translations);
        }

        int applied = 0;
        for (Map.Entry<String, Translation> entry : translations.entrySet()) {
            SlimefunItem slimefunItem = SlimefunItem.getById(entry.getKey());
            if (slimefunItem != null && translate(slimefunItem.getItem(), entry.getValue())) {
                applied++;
            }
        }

        plugin.getLogger().info("Traduzione italiana configurabile: " + applied + " oggetti tradotti.");
    }

    private static void load(JavaPlugin plugin, String file, Map<String, Translation> translations) {
        String resourcePath = "translations/it/ExoticGarden/" + file;
        File externalFile = new File(plugin.getDataFolder(), resourcePath);
        if (!externalFile.exists()) {
            plugin.saveResource(resourcePath, false);
        }

        try (InputStream stream = new FileInputStream(externalFile)) {

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String id = null;
                Translation translation = null;
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.matches("  [A-Z0-9_]+:")) {
                        id = line.substring(2, line.length() - 1);
                        translation = new Translation();
                        translations.put(id, translation);
                    } else if (translation != null && line.startsWith("    name: ")) {
                        translation.name = scalar(line.substring(10));
                    } else if (translation != null && line.startsWith("    - ")) {
                        translation.lore.add(scalar(line.substring(6)));
                    }
                }
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Impossibile leggere la traduzione configurabile " + externalFile, ex);
        }
    }

    private static String scalar(String value) {
        if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1).replace("''", "'");
        }
        return value;
    }

    private static boolean translate(ItemStack item, Translation translation) {
        if (item == null || translation.name == null) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        meta.setDisplayName(ChatColors.color(translation.name));
        if (!translation.lore.isEmpty()) {
            List<String> lore = new ArrayList<>(translation.lore.size());
            for (String line : translation.lore) {
                lore.add(ChatColors.color(line));
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return true;
    }

    private static final class Translation {
        private String name;
        private final List<String> lore = new ArrayList<>();
    }
}
