package org.oreo.siege_ladders.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemManager {

    private static JavaPlugin plugin;
    public static ItemStack siegeLadder;

    /**
     * Item initialisation
     */
    public static void init(JavaPlugin pluginInstance){
        plugin = pluginInstance;
        createSiegeLadder();
    }

    /**
     * Creates the item
     */
    private static void createSiegeLadder(){
        siegeLadder = createUniqueSiegeLadder();
    }

    /**
     * @return the item
     * Makes the siege ladder item , gives it the enchantment glow description and lore
     */
    public static ItemStack createUniqueSiegeLadder(){
        ItemStack item = new ItemStack(Material.LADDER, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§eSiege Ladder");

            List<String> lore = new ArrayList<>();
            lore.add("§7Used to climb up enemy walls");
            lore.add("§5\"Oreo laddr\""); //The funni
            meta.setLore(lore);

            meta.addEnchant(Enchantment.LUCK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);//to add the enchant glint but not have it be visible

            // Add a unique identifier to make the item non-stackable
            PersistentDataContainer data = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "unique_id");
            data.set(key, PersistentDataType.STRING, UUID.randomUUID().toString());

            item.setItemMeta(meta);
        }
        return item;
    }
}
