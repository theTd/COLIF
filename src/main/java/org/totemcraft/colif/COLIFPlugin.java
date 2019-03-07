package org.totemcraft.colif;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class COLIFPlugin extends JavaPlugin {
    private ItemStack itemModel;
    private CoreProtectAPI coreProtectAPI;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        generateItemModel();
        assert itemModel != null;

        try {
            coreProtectAPI = getCoreProtect();
            if (coreProtectAPI == null) throw new RuntimeException();
        } catch (Throwable e) {
            throw new RuntimeException("cannot initialize CoreProtectAPI");
        }

    }

    @Override
    public void onDisable() {
    }

    private CoreProtectAPI getCoreProtect() {
        Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");

        // Check that CoreProtect is loaded
        if (!(plugin instanceof CoreProtect)) {
            return null;
        }

        // Check that the API is enabled
        CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (!CoreProtect.isEnabled()) {
            return null;
        }

        // Check that a compatible version of the API is loaded
        if (CoreProtect.APIVersion() < 6) {
            return null;
        }

        return CoreProtect;
    }

    private void generateItemModel() {
        ConfigurationSection itemSection;
        if (!getConfig().isSet("item") || (itemSection = getConfig().getConfigurationSection("item")) == null)
            throw new RuntimeException("item configuration not found");

        String mat = itemSection.getString("material", null);
        if (mat == null) throw new RuntimeException("item material missing");
        Material material = Material.getMaterial(mat.toUpperCase());
        if (material == null) throw new RuntimeException("material called " + mat + " not found, please refer to " +
                "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html");

        String name = itemSection.getString("name", "查询方块");
        name = ChatColor.translateAlternateColorCodes('&', name);
        if (!name.startsWith(ChatColor.COLOR_CHAR + "")) name = ChatColor.RESET + name;

        List<String> lore = itemSection.getStringList("lore");
        if (lore == null) lore = new ArrayList<>();
        ListIterator<String> ite = lore.listIterator();
        while (ite.hasNext()) {
            String line = ite.next();
            line = ChatColor.translateAlternateColorCodes('&', line);
            if (!line.startsWith(ChatColor.COLOR_CHAR + "")) line = ChatColor.RESET + line;
            ite.set(line);
        }

        itemModel = new ItemStack(material);
        ItemMeta meta = itemModel.getItemMeta();
        if (meta == null) throw new RuntimeException("material " + material + " does not have item meta");
        meta.setDisplayName(name);
        meta.setLore(lore);
        itemModel.setItemMeta(meta);
    }
}
