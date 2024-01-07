package me.fullpage.manticlib.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.fullpage.manticlib.ManticLib;
import me.fullpage.manticlib.builders.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

import static org.apache.commons.lang.Validate.notNull;

public class SkullUtils {

    private static final Material skull;

    private static Class<?> PlayerProfile;
    private static Method createPlayerProfile;
    private static Method getTexturesMethod;
    private static Method setSkinMethod;
    private static Method setOwnerProfile;


    static {
        Material material = Material.matchMaterial("SKULL_ITEM");
        if (material == null) {
            skull = Material.matchMaterial("PLAYER_HEAD");
        } else {
            skull = material;
        }
        try {
            PlayerProfile = Class.forName("org.bukkit.profile.PlayerProfile");
        } catch (ClassNotFoundException e) {
            PlayerProfile = null;
        }
        try {
            createPlayerProfile = Bukkit.class.getDeclaredMethod("createPlayerProfile", UUID.class, String.class);
            if (PlayerProfile != null) {
                getTexturesMethod = PlayerProfile.getDeclaredMethod("getTextures");
                setSkinMethod = getTexturesMethod.getReturnType().getDeclaredMethod("setSkin", URL.class);
                setOwnerProfile = SkullMeta.class.getDeclaredMethod("setOwnerProfile", PlayerProfile);
            }
        } catch (NoSuchMethodException e) {
            createPlayerProfile = null;
            getTexturesMethod = null;
            setSkinMethod = null;
            setOwnerProfile = null;
        }
    }


    private static final HashMap<String, ItemStack> skullCache = new HashMap<>();

    public static ItemStack getSkull(String base64) {
        if (skullCache.containsKey(base64)) {
            return skullCache.get(base64);
        }
        ItemStack head = new ItemBuilder(skull).durability(3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        Field profileField;
        try {
            Object newProfile = createNewProfile(base64);
            if (newProfile == null) {
                ManticLib.get().getLogger().warning("Failed to create profile for base64: " + base64);
                return null;
            }
            if (setOwnerProfile != null) {
                setOwnerProfile.invoke(meta, newProfile);
            } else {
                profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, newProfile);
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
        head.setItemMeta(meta);
        skullCache.put(base64, head);
        return head;
    }

    private static Object createNewProfile(String base64) { // TODO Convert to our NMSLib
        if (PlayerProfile == null) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            profile.getProperties().put("textures", new Property("textures", base64));
            return profile;
        }
        URL urlFromBase64 = getUrlFromBase64(base64);
        if (urlFromBase64 == null) {
            return null;
        }

        try {
            Object profile = createPlayerProfile.invoke(null, UUID.randomUUID(), "");
            Object textures = getTexturesMethod.invoke(profile);
            setSkinMethod.invoke(textures, urlFromBase64);
            return profile;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static URL getUrlFromBase64(String base64) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64);
            String jsonString = new String(decoded, StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return new URL(jsonObject.getAsJsonObject("textures")
                    .getAsJsonObject("SKIN")
                    .get("url")
                    .getAsString());
        } catch (Throwable e) {
            return null;
        }
    }

    private static final HashMap<UUID, ItemStack> headCache = new HashMap<>();

    public static ItemStack getHead(UUID id, String name) {
        ItemStack head = new ItemBuilder(skull).durability(3);

        ItemStack itemStack = headCache.get(id);
        if (itemStack != null) {
            return itemStack;
        }

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        try {
            try {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
                if (offlinePlayer != null) {
                    meta.setOwningPlayer(offlinePlayer);
                }
            } catch (NullPointerException e) {
                // from not resetting player data
                return head;
            }
        } catch (NoSuchMethodError e) {
            meta.setOwner(name);
        }
        head.setItemMeta(meta);
        headCache.put(id, head);
        return head;
    }

    public static ItemStack getHead(Player player) {
        UUID id = player.getUniqueId();
        ItemStack head = new ItemBuilder(skull).durability(3);
        notNull(id, "id");
        ItemStack itemStack = headCache.get(id);
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
