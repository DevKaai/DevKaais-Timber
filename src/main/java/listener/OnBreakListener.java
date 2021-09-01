package listener;

import de.devkaai.timber.Timber;
import org.bukkit.*;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.Plugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
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
                        Player player = event.getPlayer();
                        cutTree(mode, blockLocation, configSettings, materialBlock, player);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void cutTree(String mode, Location blockLocation, ConfigurationSection configSettings, Material materialBlock, Player player) {
        switch (mode) {
            case "PILLAR" -> pillarAlgorithms(blockLocation, configSettings, materialBlock, player);
            //case "EDGE" -> edgeAlgorithms(blockLocation, configSettings, materialBlock);
            default -> plugin.getLogger().warning("Invalid config.yml! at mode: '" + configSettings.getString("mode") + "'");
        }
    }

    private void pillarAlgorithms(Location blockLocation, ConfigurationSection configSettings, Material materialBlock, Player player) {
        int distanceHeightUp = (configSettings.getInt("distance.height")-1) - configSettings.getInt("distance.height_offset");
        int distanceHeightDown = configSettings.getInt("distance.height_offset");
        int distanceWidth = configSettings.getInt("distance.width");
        int blockX = blockLocation.getBlockX();
        int blockY = blockLocation.getBlockY();
        int blockZ = blockLocation.getBlockZ();

        World world = player.getWorld();

        List<Point> blockXZ = new ArrayList<>();
        blockXZ.add(new Point(blockX, blockZ));

        for (int i = 0; i <= distanceWidth; i++) {
            Location blockUp = new Location(world, blockX, blockY, blockZ - i);
            Location blockRight = new Location(world, blockX + i, blockY, blockZ);
            Location blockDown = new Location(world, blockX, blockY, blockZ + i);
            Location blockLeft = new Location(world, blockX - i, blockY, blockZ);

            int tempBlockX; int tempBlockZ;

            tempBlockX = blockUp.getBlockX(); tempBlockZ = blockUp.getBlockZ();
            while (!(tempBlockX == blockRight.getBlockX() && tempBlockZ == blockRight.getBlockZ())) {
                tempBlockX++; tempBlockZ++;
                blockXZ.add(new Point(tempBlockX, tempBlockZ));
            }
            tempBlockX = blockRight.getBlockX(); tempBlockZ = blockRight.getBlockZ();
            while (!(tempBlockX == blockDown.getBlockX() && tempBlockZ == blockDown.getBlockZ())) {
                tempBlockX--; tempBlockZ++;
                blockXZ.add(new Point(tempBlockX, tempBlockZ));
            }
            tempBlockX = blockDown.getBlockX(); tempBlockZ = blockDown.getBlockZ();
            while (!(tempBlockX == blockLeft.getBlockX() && tempBlockZ == blockLeft.getBlockZ())) {
                tempBlockX--; tempBlockZ--;
                blockXZ.add(new Point(tempBlockX, tempBlockZ));
            }
            tempBlockX = blockLeft.getBlockX(); tempBlockZ = blockLeft.getBlockZ();
            while (!(tempBlockX == blockUp.getBlockX() && tempBlockZ == blockUp.getBlockZ())) {
                tempBlockX++; tempBlockZ--;
                blockXZ.add(new Point(tempBlockX, tempBlockZ));
            }
        }

        for (Point point : blockXZ) {
            //plugin.getLogger().info(point.toString());

            if (cutTreePillar(new Location(world, point.x, blockY, point.y), blockLocation, materialBlock, player, distanceHeightUp, distanceHeightDown)) {
                break;
            }
        }
    }

    private boolean cutTreePillar(Location blockLocation, Location mainBlockLocation, Material materialBlock, Player player, int distanceHeightUp, int distanceHeightDown) {
        Location blockLocationTemp = new Location(blockLocation.getWorld(), blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ());

        for (int distance = 0; distance <= distanceHeightDown; distance++) {
            int result = breakBlock(materialBlock, player, blockLocation, mainBlockLocation, true);
            if (result == 1) {
                return  true;
            } else if(result == 2) {
                break;
            }

            blockLocation.add(0.0, -1.0, 0.0);
        }

        for (int distance = 0; distance < distanceHeightUp; distance++) {
            blockLocationTemp.add(0.0D, 1.0D, 0.0D);
            int result = breakBlock(materialBlock, player, blockLocationTemp, mainBlockLocation, false);
            if (result == 1) {
                return  true;
            } else if(result == 2) {
                break;
            }
        }
        return  false;
    }

    private int breakBlock(Material materialBlock, Player player, Location blockLocationTemp, Location mainBlockLocation, boolean bypassFirstBlock) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        Damageable handItemDamageable = (Damageable) handItem.getItemMeta();
        int currentDamage = handItemDamageable.getDamage();
        int maxDamage = handItem.getType().getMaxDurability() -1;

        if (blockLocationTemp.getBlock().getType().equals(materialBlock)) {

            if (!(currentDamage < maxDamage)) return 1;

            if (!bypassFirstBlock || !mainBlockLocation.equals(blockLocationTemp)) {
                blockLocationTemp.getBlock().breakNaturally(handItem);

                double unbreakingLevel = handItem.getEnchantmentLevel(Enchantment.DURABILITY);
                if (unbreakingLevel > 0) {
                    double reducesDurability = (100/unbreakingLevel)/100;
                    double random = Math.random();
                    if (reducesDurability > random) {
                        handItemDamageable.setDamage(currentDamage + 1);
                    }
                } else {
                    handItemDamageable.setDamage(currentDamage + 1);
                }
                handItem.setItemMeta(handItemDamageable);
            }

        } else return 2;

        return 0;
    }

//    private void edgeAlgorithms(Location blockLocation, ConfigurationSection configSettings, Material materialBlock) {
//        // Ecken Mitnehmen wäre Toll
//    }
}