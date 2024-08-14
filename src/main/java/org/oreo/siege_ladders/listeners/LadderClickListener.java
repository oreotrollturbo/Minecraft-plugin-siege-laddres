package org.oreo.siege_ladders.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.oreo.siege_ladders.Siege_ladders;
import phonon.nodes.war.FlagWar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LadderClickListener implements Listener {

    private final Siege_ladders plugin;
    private final Map<Location, List<Location>> ladderMap = new HashMap<>();

    public LadderClickListener(Siege_ladders plugin) {
        this.plugin = plugin; // get the plugin
    }

    /**
     * Handles all the ladder logic
     *
     * @param e the event
     */
    @EventHandler
    public void onLadderPlaced(PlayerInteractEvent e) {

        Action act = e.getAction();
        if (act == Action.LEFT_CLICK_BLOCK) {

            Block block = e.getClickedBlock(); // Get the block placed
            Player player = e.getPlayer();
            assert block != null;
            World world = block.getWorld(); // Get the world
            Location initialLocation = new Location(block.getWorld(),block.getX(),block.getY() + 1, block.getZ()); // Get the block's location

            boolean war = FlagWar.INSTANCE.getEnabled$nodes();

            if (isHoldingSiegeLadder(player)) {
                // Make the player place a siegeLadder and the block placed is a ladder
                e.setCancelled(true);

                if (!war) {
                    player.sendMessage(ChatColor.RED + "You can only use siege ladders during war");
                    return;
                }

                if (!e.getBlockFace().equals(BlockFace.UP)){
                    player.sendMessage(ChatColor.RED + "You can only use this function on the top of a block");
                    return;
                }

                player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1); // Make sure the item is consumed even when cancelled
                BlockFace playerFacing = getOppositeFace(player.getFacing()); // Get the direction the player is facing

                int ladderDelay = this.plugin.getConfig().getInt("supported-ladder-delay"); // Get from the config
                int maxLadders = this.plugin.getConfig().getInt("supported-max-ladders"); // Get from the config
                int maxLaddersPenalty = this.plugin.getConfig().getInt("supported-max-ladder-penalty"); // Get from the config
                int maxSolidBlocksBroken = this.plugin.getConfig().getInt("supported-max-solid-blocks-broken"); // Get from the config

                if (block.getBlockData().getMaterial().equals(Material.OAK_PLANKS)){
                    maxLadders = maxLadders - (maxLaddersPenalty-1);
                }

                int finalMaxLadders = maxLadders;
                new BukkitRunnable() { // Essentially a new thread
                    private int timer = 1;
                    private int solidBlocksBroken = 0;
                    private final List<Location> ladders = new ArrayList<>();
                    // Set the config values

                    @Override
                    public void run() {
                        synchronized (ladderMap) { // Synchronize access to ladderMap
                            // Add the original ladder placed to the list
                            ladders.add(initialLocation);

                            Location supportBlockLocation = new Location(world, block.getX(), block.getY() + timer, block.getZ()); // Get a new location
                            Block supportBlock = supportBlockLocation.getBlock();

                            Block ladderBlock = setLadderBlock(supportBlockLocation,playerFacing,timer);

                            if (timer >= finalMaxLadders || solidBlocksBroken >= maxSolidBlocksBroken || isStorageBlock(supportBlock)
                                    || (timer > 1 && noBlockBelow(supportBlockLocation))) { // Stop after placing 10 ladders
                                // Or if the block isn't solid

                                if (!isStorageBlock(supportBlock)) {
                                    if (supportBlock.getType().isSolid() || timer > 1 && noBlockBelow(supportBlockLocation)) {
                                        world.playSound(supportBlockLocation, Sound.ITEM_SHIELD_BREAK, 1f, 5f);
                                    }
                                    supportBlock.setType(Material.AIR);
                                }
                                cancel();
                                ladderMap.put(initialLocation, new ArrayList<>(ladders)); // Store a copy of ladders
                                return;
                            }

                            if (supportBlock.getBlockData().getMaterial().isSolid()) {
                                solidBlocksBroken++;
                                world.playSound(supportBlockLocation, Sound.BLOCK_COPPER_BREAK, 1f, 1f);
                            }

                            supportBlock.setType(Material.OAK_PLANKS);
                            world.playSound(supportBlockLocation, Sound.BLOCK_WOOD_PLACE, 1f, 0.4f);
                            ladders.add(supportBlockLocation);

                            assert ladderBlock != null;
                            ladderBlock.setType(Material.LADDER); // Place the new support block
                            Directional newLadderData = (Directional) ladderBlock.getBlockData();
                            newLadderData.setFacing(playerFacing); // Set the facing direction based on the player's facing direction
                            ladderBlock.setBlockData(newLadderData);// Add new block location to the list
                            ladders.add(ladderBlock.getLocation()); // Add new block location to the list

                            timer++; // Increment the timer

                        }
                    }
                }.runTaskTimer(plugin, ladderDelay, 20);
            }
        }
    }

    /**
     * Gets the map of all siege ladders placed
     *
     * @return the ladder map
     */
    public Map<Location, List<Location>> getLadderMap() {
        return ladderMap;
    }

    /**
     * Checks if the player is holding a ladder and that ladder has the correct
     * enchantments
     *
     * @param player the player
     * @return boolean
     */
    private boolean isHoldingSiegeLadder(Player player) { // Method to check if the player is holding the siegeLadder
        return player.getInventory().getItemInMainHand().getType().equals(Material.LADDER)
                && player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.LUCK); // Checks for the
        // luck enchantment
    }

    /**
     * Checks if the block to be "broken" is a storage block
     *
     * @param block Block
     * @return boolean
     */
    private boolean isStorageBlock(Block block) { // To stop people from nuking storages with this
        return block.getBlockData().getMaterial().equals(Material.CHEST)
                || block.getBlockData().getMaterial().equals(Material.BARREL)
                || block.getBlockData().getMaterial().equals(Material.TRAPPED_CHEST);
    }

    /**
     * Simply checks if the block below it is a ladder
     * This is so that players can break the top ladder to stop the thing from
     * deploying
     *
     * @param location the location of the ladder to be placed
     * @return whether the block below it is a ladder
     */
    private boolean noBlockBelow(Location location) {
        Location blockBelow = location.clone().subtract(0, 1, 0); // Get the location directly below
        Block block = blockBelow.getBlock();
        return !block.getType().isSolid(); // Return true if the block below is not solid
    }

    /**
     * @param face the block face
     * Sets the opposite face of which you got because player.getfacing() is the opposite for blocks
     *            so ladders would be inverted
     */
    private static BlockFace getOppositeFace(BlockFace face) {
        switch (face) {
            case NORTH:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.NORTH;
            case EAST:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.EAST;
            case UP:
                return BlockFace.DOWN;
            case DOWN:
                return BlockFace.UP;
            default:
                return face; // If the BlockFace is not one of the main six, return it as is
        }
    }

    private static Block setLadderBlock(Location supportBlocklocation,BlockFace face,int timer){

        Location ladderLocation;
        int x = supportBlocklocation.getBlockX();
        int y = supportBlocklocation.getBlockY();
        int z = supportBlocklocation.getBlockZ();

        switch (face) {
            case NORTH:
                ladderLocation = new Location(supportBlocklocation.getWorld(),x,y,z - 1);
                return ladderLocation.getBlock();
                //+1 Z
            case SOUTH:
                ladderLocation = new Location(supportBlocklocation.getWorld(),x,y,z + 1);
                return ladderLocation.getBlock();
                //-1 Z
            case EAST:
                ladderLocation = new Location(supportBlocklocation.getWorld(),x + 1,y,z);
                return ladderLocation.getBlock();
                //+1 X
            case WEST:
                ladderLocation = new Location(supportBlocklocation.getWorld(),x - 1,y,z);
                return ladderLocation.getBlock();
                //-1 X
            default:
                return null;
        }
    }
}
