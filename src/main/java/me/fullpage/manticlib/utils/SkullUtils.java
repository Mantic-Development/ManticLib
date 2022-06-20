package me.fullpage.manticlib.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.tr7zw.changeme.nbtapi.NBTGameProfile;
import me.fullpage.manticlib.builders.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.UUID;

import static org.apache.commons.lang.Validate.notNull;

public class SkullUtils {

    private static Material skull;

    static {
        Material material = Material.matchMaterial("PLAYER_HEAD");
        if (material == null) {
            skull = Material.matchMaterial("SKULL_ITEM");
        } else {
            skull = material;
        }
    }


    private static final HashMap<String, ItemStack> skullCache = new HashMap<>();

    public static ItemStack getSkull(String base64) {
        if (skullCache.containsKey(base64)) {
            return skullCache.get(base64);
        }
        final ItemStack head = new ItemBuilder(skull);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", base64));
        Field profileField;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        head.setItemMeta(meta);
        skullCache.put(base64, head);
        return head;
    }

    private static final HashMap<UUID, ItemStack> headCache = new HashMap<>();

    public static ItemStack getHead(UUID id, String name) {
        final ItemStack head = new ItemBuilder(skull);

        final ItemStack itemStack = headCache.get(id);
        if (itemStack != null) {
            return itemStack;
        }

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        try {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
        } catch (NoSuchMethodError e) {
            meta.setOwner(name);
        }
        head.setItemMeta(meta);
        headCache.put(id, head);

        return head;
    }

    public static ItemStack getHead(Player player) {
        final UUID id = player.getUniqueId();
        final ItemStack head = new ItemBuilder(skull);
        notNull(id, "id");
        final ItemStack itemStack = headCache.get(id);
        if (itemStack != null) {
            return itemStack;
        }

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        try {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
        } catch (NoSuchMethodError e) {
            meta.setOwner(player.getName());
        }
        head.setItemMeta(meta);

        headCache.put(player.getUniqueId(), head);
        return head;
    }

}
