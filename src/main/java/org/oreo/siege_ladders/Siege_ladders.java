package org.oreo.siege_ladders;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.oreo.siege_ladders.commands.ClearAllSiegeLadders;
import org.oreo.siege_ladders.commands.GetSiegeLadder;
import org.oreo.siege_ladders.items.ItemManager;
import org.oreo.siege_ladders.listeners.LadderBreakListener;
import org.oreo.siege_ladders.listeners.LadderClickListener;
import org.oreo.siege_ladders.listeners.LadderPlaceListener;

import java.util.List;
import java.util.Map;

public final class Siege_ladders extends JavaPlugin {

    /**
     * Synchronises the siege ladder list
     */
    LadderPlaceListener ladderPlaceListener = new LadderPlaceListener(this); //Syncs both of the lists for ladders
    LadderClickListener ladderClickListener = new LadderClickListener(this); //Syncs both of the lists for ladders

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Siege ladders loaded successfully"); // Just to make sure

        getCommand("siege-ladder").setExecutor(new GetSiegeLadder()); //Register the command to get a siegeLadder
        getCommand("remove-siege-ladders").setExecutor(new ClearAllSiegeLadders(ladderPlaceListener)); //Command to clear all siege ladders

        ItemManager.init(this); // Register items

        getServer().getPluginManager().registerEvents(ladderPlaceListener, this); //Register the ladder events
        getServer().getPluginManager().registerEvents(new LadderBreakListener(ladderPlaceListener,ladderClickListener), this);
        getServer().getPluginManager().registerEvents(ladderClickListener, this);
        //Add default config
        saveDefaultConfig();
    }

    /**
     * Deletes all siege ladders when the server is disabled
     */
    @Override
    public void onDisable() {

        getLogger().info("Siege ladders unloading"); // Breaking all siege ladders when the server shuts down
        for (Map.Entry<Location, List<Location>> entry : ladderPlaceListener.getLadderMap().entrySet()) {
            List<Location> ladderList = entry.getValue();
            // Break all ladders in every list
            for (Location ladderLocation : ladderList) {
                ladderLocation.getBlock().setType(Material.AIR);
            }
        }
        getLogger().info("unloaded"); // Just to make sure
    }
}
