package org.oreo.siege_ladders.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;
import java.util.Map;

public class LadderBreakListener implements Listener {

    private final LadderPlaceListener ladderListener;
    private final LadderClickListener ladderClickListener;

    public LadderBreakListener(LadderPlaceListener ladderListener, LadderClickListener ladderClickListener) {
        this.ladderListener = ladderListener;
        this.ladderClickListener = ladderClickListener;
    }

    /**
     * @param e the block break event
     * When the ladder is broken it checks if it's in a "ladder list" and breaks all of them
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void LadderBreak(BlockBreakEvent e){
        Block block = e.getBlock();

        if ((block.getBlockData().getMaterial().equals(Material.LADDER) || block.getBlockData().getMaterial().equals(Material.OAK_PLANKS))
                && !e.isCancelled()){
            Location brokenLadderLocation = block.getLocation();
            Map<Location, List<Location>> ladderMap = ladderListener.getLadderMap();
            Map<Location, List<Location>> ladderClickMap = ladderClickListener.getLadderMap();

            for (Map.Entry<Location, List<Location>> entry : ladderMap.entrySet()) {
                List<Location> ladderList = entry.getValue();
                if (ladderList.contains(brokenLadderLocation)) {
                    // Break all ladders in the list
                    for (Location ladderLocation : ladderList) {
                        ladderLocation.getBlock().setType(Material.AIR);
                    }

                    // Remove the entire list of ladders
                    ladderMap.remove(entry.getKey());

                    // No need to continue searching, exit the loop
                    break;
                }
            }

            for (Map.Entry<Location, List<Location>> entry : ladderClickMap.entrySet()) {
                List<Location> ladderList = entry.getValue();
                if (ladderList.contains(brokenLadderLocation)) {
                    // Break all ladders in the list
                    for (Location ladderLocation : ladderList) {
                        ladderLocation.getBlock().setType(Material.AIR);
                    }

                    // Remove the entire list of ladders
                    ladderMap.remove(entry.getKey());

                    // No need to continue searching, exit the loop
                    break;
                }
            }
        }
    }
}
