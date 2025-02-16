package io.github.KarolisLipinskas.treeSpirit;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PlayerTracker {
    private final JavaPlugin plugin;
    private final ChallengeManager challengeManager;

    public PlayerTracker(JavaPlugin plugin, ChallengeManager challengeManager) {
        this.plugin = plugin;
        this.challengeManager = challengeManager;
        startTracking();
    }

    private void startTracking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getGameMode() != GameMode.ADVENTURE && !isInsideDetectionRadius(player)) {
                        player.sendMessage("You left the safe zone!");
                        //player.setHealth(0); // Kill player
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private boolean isInsideDetectionRadius(Player player) {
        return !Collections.disjoint(challengeManager.getDetectionRadius(), getBlocksInBoundingBox(player));
    }

    public static Set<Location> getBlocksInBoundingBox(Player player) {
        Set<Location> blockLocations = new HashSet<>();
        World world = player.getWorld();
        BoundingBox box = player.getBoundingBox();

        // Get the min and max coordinates (rounding to whole block values)
        int minX = (int) Math.floor(box.getMinX());
        int minY = (int) Math.floor(box.getMinY());
        int minZ = (int) Math.floor(box.getMinZ());

        int maxX = (int) Math.floor(box.getMaxX());
        int maxY = (int) Math.floor(box.getMaxY());
        int maxZ = (int) Math.floor(box.getMaxZ());

        // Iterate over all block positions within the bounding box
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location blockLocation = world.getBlockAt(x, y, z).getLocation();
                    blockLocations.add(blockLocation);
                }
            }
        }
        return blockLocations;
    }
}