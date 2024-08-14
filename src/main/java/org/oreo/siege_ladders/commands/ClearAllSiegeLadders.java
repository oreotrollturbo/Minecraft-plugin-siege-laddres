package org.oreo.siege_ladders.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.oreo.siege_ladders.items.ItemManager;
import org.oreo.siege_ladders.listeners.LadderPlaceListener;
import phonon.nodes.Nodes;
import phonon.nodes.objects.Resident;
import phonon.nodes.objects.Town;

import java.util.List;
import java.util.Map;

public class ClearAllSiegeLadders implements CommandExecutor {

    private final LadderPlaceListener ladderListener;

    public ClearAllSiegeLadders(LadderPlaceListener ladderListener) {
        this.ladderListener = ladderListener;
    }

    /**
     * Checks if the player is Oped and deletes all siege ladders
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender.isOp()){
            sender.sendMessage("Removing all siege ladders");
            for (Map.Entry<Location, List<Location>> entry : ladderListener.getLadderMap().entrySet()) {
                List<Location> ladderList = entry.getValue();
                // Break all ladders in every list
                for (Location ladderLocation : ladderList) {
                    ladderLocation.getBlock().setType(Material.AIR);
                }
            }
        }else {
            sender.sendMessage("§c You don't have permission to use this command");
        }

        return true;
    }
}
