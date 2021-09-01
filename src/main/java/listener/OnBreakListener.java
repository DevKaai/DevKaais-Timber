package listener;

import de.devkaai.timber.Timber;
import org.bukkit.*;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.Plugin;

import java.util.Locale;

public class OnBreakListener implements Listener {
    private final Configuration config = Timber.config;
    private final Plugin plugin;

    public OnBreakListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().getGameMode().equals(GameMode.SURVIVAL) || !config.getBoolean("Settings.onSneak") == event.getPlayer().isSneaking() || !event.getPlayer().hasPermission("timber.use")) {
            return;
        }

        for (Material tool : Timber.tools) {
            if (event.getPlayer().getInventory().getItemInMainHand().getType().equals(tool)) {
                for (Material block : Timber.blocks) {
                    if (event.getBlock().getType().equals(block)) {
                        Location blockLocation = event.getBlock().getLocation();
                        String mode = config.getString("Settings.mode").toUpperCase(Locale.ENGLISH);
                        ConfigurationSection configSettings = config.getConfigurationSection("Settings");
                        Material materialBlock = event.getBlock().getType();
                        ItemStack itemHand = event.getPlayer().getInventory().getItemInMainHand();
                        cutTree(mode, blockLocation, configSettings, materialBlock, itemHand);

                        Damageable handItemDamageable = (Damageable) event.getPlayer().getInventory().getItemInMainHand().getItemMeta();
                        break;
                    }
                }
                break;
            }
        }
    }

    private void cutTree(String mode, Location blockLocation, ConfigurationSection configSettings, Material materialBlock, ItemStack handItem) {
        switch (mode) {
            case "PILLAR" -> pillarAlgorithms(blockLocation, configSettings, materialBlock, handItem);
            case "EDGE" -> edgeAlgorithms(blockLocation, configSettings, materialBlock);
            default -> plugin.getLogger().warning("Invalid config.yml! at mode: '" + configSettings.getString("mode") + "'");
        }
    }

    private void pillarAlgorithms(Location blockLocation, ConfigurationSection configSettings, Material materialBlock, ItemStack handItem) {
        int distanceHeightUp = (configSettings.getInt("distance.height")-1) - configSettings.getInt("distance.height_offset");
        int distanceHeigtDown = configSettings.getInt("distance.height_offset");
        int distanceWidth = configSettings.getInt("distance.width");
        Location blockLocationTemp = new Location(blockLocation.getWorld(), blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ());

        for (int i = 0; i < distanceWidth; i++) {

        }

        cutTreePillar(blockLocation, materialBlock, handItem, distanceHeightUp, distanceHeigtDown, blockLocationTemp);


    }

    private void cutTreePillar(Location blockLocation, Material materialBlock, ItemStack handItem, int distanceHeightUp, int distanceHeigtDown, Location blockLocationTemp) {
        for (int distance = 0; distance < distanceHeigtDown; distance++) {
            blockLocation.add(0.0, -1.0, 0.0);
            if (breakBlocks(materialBlock, handItem, blockLocation)) break;
        }

        for (int distance = 0; distance < distanceHeightUp; distance++) {
            blockLocationTemp.add(0.0D, 1.0D, 0.0D);
            if (breakBlocks(materialBlock, handItem, blockLocationTemp)) break;
        }
    }

    private boolean breakBlocks(Material materialBlock, ItemStack handItem, Location blockLocationTemp) {
        Damageable handItemDamageable = (Damageable) handItem.getItemMeta();
        int currentDamage = handItemDamageable.getDamage();
        int maxDamage = handItem.getType().getMaxDurability() -1;

        if (blockLocationTemp.getBlock().getType().equals(materialBlock) && currentDamage < maxDamage) {
            blockLocationTemp.getBlock().breakNaturally(handItem);
            handItemDamageable.setDamage(currentDamage + 1);
            handItem.setItemMeta(handItemDamageable);
            //plugin.getLogger().info("Haltbarkeit: " + handItemDamageable.getDamage() + "/" + maxDamage);
        } else return true;
        return false;
    }

    private void edgeAlgorithms(Location blockLocation, ConfigurationSection configSettings, Material materialBlock) {

    }


}
