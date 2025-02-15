package io.github.KarolisLipinskas.treeSpirit;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

public final class TreeSpirit extends JavaPlugin {
    public ChallengeManager challengeManager;
    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getLogger().info("TreeSpirit plugin has been enabled!");

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        challengeManager = new ChallengeManager(this);
        getServer().getPluginManager().registerEvents(challengeManager, this);
        getCommand("border").setExecutor(new BorderCommand(challengeManager));

    }

    public void initializeChallengeStart(Block startBlock) {
        getConfig().set("challengeStarted", true);
        saveConfig();
        challengeManager.startChallenge(startBlock);
        Bukkit.broadcast(Component.text("The Challenge has started for everyone!"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        challengeManager.saveConfig();
    }
}
