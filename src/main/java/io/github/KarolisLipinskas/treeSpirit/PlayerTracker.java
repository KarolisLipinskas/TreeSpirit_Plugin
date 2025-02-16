package io.github.KarolisLipinskas.treeSpirit;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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
                    if (player.getGameMode() != GameMode.ADVENTURE && !isInsideDetectionRadius(player.getLocation(), player)) {
                        player.sendMessage("You left the safe zone!");
                        //player.setHealth(0); // Kill player
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private boolean isInsideDetectionRadius(Location playerLoc, Player player) {
        /*player.sendMessage("your loc: " + player.getLocation());
        player.sendMessage("Detection block count: " + challengeManager.getDetectionRadius().size());
        player.sendMessage("is player inside detection radius: " + challengeManager.getDetectionRadius().contains(playerLoc));
        Location roundedPlayerLoc = playerLoc.clone();
        player.sendMessage("player location block: " + roundedPlayerLoc.getBlock().getLocation());*/
        return challengeManager.getDetectionRadius().contains(playerLoc.getBlock().getLocation());
    }
}