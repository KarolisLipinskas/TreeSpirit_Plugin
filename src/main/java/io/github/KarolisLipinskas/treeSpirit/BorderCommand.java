package io.github.KarolisLipinskas.treeSpirit;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class BorderCommand implements CommandExecutor {
    private final ChallengeManager challengeManager;
    private final Set<Player> activePlayers = new HashSet<>();
    private BukkitRunnable borderTask = null;

    public BorderCommand(ChallengeManager challengeManager) {
        this.challengeManager = challengeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length != 1) return false;

        if (args[0].equalsIgnoreCase("true")) {
            enableBorder(player);
            return true;
        } else if (args[0].equalsIgnoreCase("false")) {
            disableBorder(player);
            return true;
        }
        return false;
    }

    private void enableBorder(Player player) {
        activePlayers.add(player);
        player.sendMessage("Border enabled!");

        if (borderTask == null) {
            startBorderTask();
        }
    }

    private void disableBorder(Player player) {
        activePlayers.remove(player);
        player.sendMessage("Border disabled!");

        if (activePlayers.isEmpty() && borderTask != null) {
            borderTask.cancel();
            borderTask = null;
        }
    }

    private void startBorderTask() {
        borderTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (activePlayers.isEmpty()) {
                    cancel();
                    borderTask = null;
                    return;
                }

                for (Player player : activePlayers) {
                    for (Location loc : challengeManager.getDetectionRadius()) {
                        player.spawnParticle(Particle.FLAME, loc.clone().add(0.5, 0.5, 0.5), 5, 0.1, 0.1, 0.1, 0);
                    }
                }
            }
        };
        borderTask.runTaskTimer(challengeManager.getPlugin(), 0L, 10L);
    }
}
