package org.oreo.siege_ladders.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.oreo.siege_ladders.items.ItemManager;

public class GetSiegeLadder implements CommandExecutor {


    /**
     * Gives the player a siege ladder if the player is OP
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)){
            sender.sendMessage("Only players can use this command");
            return true;
        }

        Player player = (Player) sender;
        if (player.isOp()){
            player.getInventory().addItem(ItemManager.siegeLadder);
            player.sendMessage("Gave you a siege ladder successfully");
        }else {
            player.sendMessage("§c You don't have permission to use this command");
        }
        return true;
    }
}
