package io.github.KarolisLipinskas.treeSpirit;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class JoinListener implements Listener {
    private final JavaPlugin plugin;
    private final LogTracker logTracker;

    public JoinListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logTracker = new LogTracker(plugin);
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore()) {
            player.setGameMode(GameMode.ADVENTURE);
            player.sendMessage("Welcome to Tree Spirit Challenge move to the existing tree or if none exist mine any log block to start");
        }

        if (getPlugin(TreeSpirit.class).getConfig().getBoolean("challengeStarted", false)) {
            //check if player is in ADVENTURE and call functions that haldle navigation to start// or tp
            if (player.getGameMode() == GameMode.ADVENTURE) {
                FileConfiguration config = plugin.getConfig();
                Location startBlock = config.getLocation("challengeBlockLoc");
                assert startBlock != null;
                player.sendMessage("The challenge has started navigate to " + startBlock.getBlockX() + " " + startBlock.getBlockY() + " " + startBlock.getBlockZ() + " block to join");
            }
        } else {
            logTracker.startTracking(player);
        }
    }
}
