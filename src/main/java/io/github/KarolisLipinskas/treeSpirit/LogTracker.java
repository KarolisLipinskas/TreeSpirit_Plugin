package io.github.KarolisLipinskas.treeSpirit;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class LogTracker {
    private final JavaPlugin plugin;
    private final HashMap<UUID, Integer> taskMap = new HashMap<>();

    public LogTracker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void startTracking(Player player) {
        if (taskMap.containsKey(player.getUniqueId())) return;

        BukkitRunnable task = new BukkitRunnable() {
            Block correctBlock = null;
            @Override
            public void run() {
                Block logBlock = getLookingAtAcceptableLog(player);
                if (logBlock != null) {
                    correctBlock = logBlock;
                    if (player.getGameMode() != GameMode.SURVIVAL) {
                        player.setGameMode(GameMode.SURVIVAL);
                    }
                } else {
                    if (player.getGameMode() != GameMode.ADVENTURE) {
                        player.setGameMode(GameMode.ADVENTURE);
                    }
                }

                if (correctBlock != null && correctBlock.getLocation().getBlock().getType() == Material.AIR) {
                    if (player.getGameMode() != GameMode.SURVIVAL) {
                        player.setGameMode(GameMode.SURVIVAL);
                    }
                    player.sendMessage("The Challenge has started");
                    ((TreeSpirit) plugin).initializeChallengeStart(correctBlock.getRelative(BlockFace.DOWN));
                    //maybe stop tracking for all players??
                    stopTracking(player);
                }
            }
        };

        Integer taskID = task.runTaskTimer(plugin, 0L, 5L).getTaskId();
        taskMap.put(player.getUniqueId(), taskID);
    }

    public void stopTracking(Player player) {
        Integer taskID = taskMap.remove(player.getUniqueId());
        if (taskID != null) {
            Bukkit.getScheduler().cancelTask(taskID);
        }
    }

    private Block getLookingAtAcceptableLog(Player player) {
        Block targetBlock = player.getTargetBlock(null, 5);
        if (!Tag.LOGS.isTagged(targetBlock.getType())) {
            return null;
        }

        Block belowBlock = targetBlock.getRelative(BlockFace.DOWN);
        Block twoBlocksBelowBlock = belowBlock.getRelative(BlockFace.DOWN);
        if (Tag.LOGS.isTagged(belowBlock.getType()) && !Tag.LOGS.isTagged(twoBlocksBelowBlock.getType())) {
            return targetBlock;
        }
        return null;
    }
}
