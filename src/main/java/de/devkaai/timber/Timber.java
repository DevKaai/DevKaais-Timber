package de.devkaai.timber;

import listener.OnBreakListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Timber extends JavaPlugin {
    public final Plugin plugin = this;
    public static Configuration config;
    public final static List<Material> blocks = new ArrayList<>();
    public final static List<Material> tools = new ArrayList<>();
    private final static List<String> errorOutput = new ArrayList<>();

    @Override
    public void onEnable() {
        getConfig();
        saveDefaultConfig();
        config = getConfig();

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new OnBreakListener(this), this);

        if (    configChecker(config.getStringList("Blocks"), blocks) |
                configChecker(config.getStringList("Tools"), tools)) {
            getLogger().warning("Invalid config.yml!");
            getLogger().warning("at " + errorOutput.get(0));
            for (int i = 1; i < errorOutput.size(); i++) {
                getLogger().warning(errorOutput.get(i));
            }
            getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    private boolean configChecker(List<String> configBlocks, List<Material> material) {
        boolean error = false;
        for (String block : configBlocks) {
            material.add(Material.getMaterial(block.toUpperCase(Locale.ENGLISH)));

            if (material.get(material.size()-1) == null) {
                errorOutput.add(block);
                error = true;
            }
        }
        return error;
    }

    @Override
    public void onDisable() {

    }
}
