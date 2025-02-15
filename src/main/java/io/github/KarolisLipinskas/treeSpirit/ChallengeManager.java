package io.github.KarolisLipinskas.treeSpirit;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ChallengeManager implements Listener {
    private final JavaPlugin plugin;
    private final Set<Location> detectionRadius = new HashSet<>();
    private final Set<Location> placedBlockList = new HashSet<>();
    private final Set<Location> activeBlockList = new HashSet<>();
    private Material challengeBlockType = null;
    private Location challengeBlockLoc;

    public ChallengeManager(JavaPlugin plugin) {
        this.plugin = plugin;

        FileConfiguration config = plugin.getConfig();
        String blockTypeString = config.getString("challengeBlock");
        if (blockTypeString != null) this.challengeBlockType = Material.matchMaterial(blockTypeString);
        this.challengeBlockLoc = config.getLocation("challengeBlockLoc");

        loadChallengeData();
    }

    public void startChallenge(Block startBlock) {
        challengeBlockType = startBlock.getType();
        challengeBlockLoc = startBlock.getLocation();
        detectionRadius.add(startBlock.getLocation());
        placedBlockList.add(startBlock.getLocation());
        activeBlockList.add(startBlock.getLocation());

        // Save to config
        FileConfiguration config = plugin.getConfig();
        config.set("challengeBlock", challengeBlockType.toString());
        config.set("challengeBlockLoc", startBlock.getLocation());
        plugin.saveConfig();

        expandDetectionArea(startBlock.getLocation(), true);

        Bukkit.broadcast(Component.text("Challenge started on " + challengeBlockType.name()));
    }

    private Set<Location> findConnections(Location location, int radius) {
        Set<Location> connections = new HashSet<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location newLoc = location.clone().add(x, y, z);
                    if (activeBlockList.contains(newLoc) && newLoc != location) {
                        connections.add(newLoc);
                    }
                }
            }
        }
        return connections;
    }

    private Set<Location> getBlocksToRemove(Location brokenBlock) {
        Set<Location> blockToRemove = new HashSet<>();
        for (Location loc : findConnections(brokenBlock, 1)) {
            boolean isConnected = false;
            Set<Location> visited = new HashSet<>();
            Queue<Location> queue = new LinkedList<>();
            queue.add(loc);
            visited.add(loc);

            while (!queue.isEmpty())
            {
                Location currentLoc = queue.poll();
                if (currentLoc.equals(challengeBlockLoc)) {
                    isConnected = true;
                    break;
                }

                for (Location loc2 : findConnections(currentLoc, 1)) {
                    if (!visited.contains(loc2) && !loc2.equals(brokenBlock)) {
                        queue.add(loc2);
                        visited.add(loc2);
                    }
                }
            }

            if (!isConnected) blockToRemove.addAll(visited);
        }
        return blockToRemove;
    }

    private void expandDetectionArea(Location center, boolean canExpand) {
        if (placedBlockList.contains(center))
            activeBlockList.add(center);

        int radius = 1;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location newLoc = center.clone().add(x, y, z);
                    detectionRadius.add(newLoc);
                    if (canExpand && placedBlockList.contains(newLoc) && !activeBlockList.contains(newLoc)) {
                        activeBlockList.add(newLoc);
                        expandDetectionArea(newLoc, true);
                    }
                }
            }
        }
    }

    private void reduceDetectionArea(Location center) {
        int radius = 1;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location newLoc = center.clone().add(x, y, z);
                    detectionRadius.remove(newLoc);
                }
            }
        }
        activeBlockList.remove(center);
        for (Location loc : findConnections(center, 2)) expandDetectionArea(loc, false);
    }

    private void removeDetection(Location brokenBlock)
    {
        Set<Location> blocksToRemove = getBlocksToRemove(brokenBlock);
        reduceDetectionArea(brokenBlock);
        for (Location loc : blocksToRemove) reduceDetectionArea(loc);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (challengeBlockType == null) return;

        Block placedBlock = event.getBlock();
        if (placedBlock.getType() == challengeBlockType) {
            placedBlockList.add(placedBlock.getLocation());
            if (detectionRadius.contains(placedBlock.getLocation())) {
                expandDetectionArea(placedBlock.getLocation(), true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (challengeBlockType == null) return;

        Block brokenBlock = event.getBlock();
        if (brokenBlock.getType() == challengeBlockType) {
            if (activeBlockList.contains(brokenBlock.getLocation())) {
                placedBlockList.remove(brokenBlock.getLocation());
                removeDetection(brokenBlock.getLocation());
            }
            else placedBlockList.remove(brokenBlock.getLocation());
        }
    }

    public void saveConfig() {
        saveLocationList("activeBlockList", activeBlockList);

        Set<Location> placedBlockListDiff = new HashSet<>(placedBlockList);
        placedBlockListDiff.removeAll(activeBlockList);
        saveLocationList("placedBlockListDiff", placedBlockListDiff);
    }

    private void saveLocationList(String path, Set<Location> locationList) {
        FileConfiguration config = plugin.getConfig();

        // Convert Set<Location> to List<Map<String, Object>>
        List<Map<String, Object>> serializedLocations = new ArrayList<>();
        for (Location loc : locationList) serializedLocations.add(loc.serialize());

        // Save the list to config
        config.set(path, serializedLocations);
        plugin.saveConfig();
    }

    private void loadChallengeData() {
        activeBlockList.addAll(loadLocationList("activeBlockList"));

        Set<Location> placedBlockListDiff = loadLocationList("placedBlockListDiff");
        placedBlockList.addAll(activeBlockList);
        placedBlockList.addAll(placedBlockListDiff);

        for (Location loc : activeBlockList) expandDetectionArea(loc, false);
    }

    private Set<Location> loadLocationList(String path) {
        FileConfiguration config = plugin.getConfig();
        Set<Location> locationList = new HashSet<>();

        List<Map<?, ?>> serializedList = config.getMapList(path);
        for (Map<?, ?> map : serializedList) {
            try {
                @SuppressWarnings("unchecked") // Suppress warning for a verified cast
                Map<String, Object> safeMap = (Map<String, Object>) map;
                locationList.add(Location.deserialize(safeMap));
            } catch (ClassCastException e) {
                plugin.getLogger().warning("Invalid location data in config: " + map);
            }
        }

        return locationList;
    }

    public Set<Location> getDetectionRadius() {
        return detectionRadius;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
