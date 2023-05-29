package me.fullpage.manticlib.utils;


import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper-class for getting UUIDs of players.
 */
public final class UUIDFetcher {

    private static HashMap<String, UUID> realUuids = new HashMap<>();

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";

    private static final Pattern UUID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"(.*?)\"");

    private UUIDFetcher() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the UUID of the searched player.
     *
     * @param player The player.
     * @return The UUID of the given player.
     */
    public static UUID getUUID(Player player) {
        return getUUID(player.getName());
    }

    /**
     * Returns the UUID of the searched player.
     *
     * @param name The name of the player.
     * @return The UUID of the given player.
     */
    public static UUID getUUID(String name) {
        final UUID uuid = realUuids.get(name);
        if (uuid == null) {
            String output = callURL(UUID_URL + name);
            Matcher m = UUID_PATTERN.matcher(output);
            if (m.find()) {
                 UUID fromString = UUID.fromString(insertDashes(m.group(1)));
                realUuids.put(name, fromString);
                return fromString;
            }
            return null;
        } else {
            return uuid;
        }
    }

    /**
     * Helper method for inserting dashes into
     * unformatted UUID.
     *
     * @return Formatted UUID with dashes.
     */
    public static String insertDashes(String uuid) {
        StringBuilder sb = new StringBuilder(uuid);
        sb.insert(8, '-');
        sb.insert(13, '-');
        sb.insert(18, '-');
        sb.insert(23, '-');
        return sb.toString();
    }

    private static String callURL(String urlStr) {
        StringBuilder sb = new StringBuilder();
        URLConnection conn;
        BufferedReader br = null;
        InputStreamReader in = null;
        try {
            conn = new URL(urlStr).openConnection();
            if (conn != null) {
                conn.setReadTimeout(60 * 1000);
            }
            if (conn != null && conn.getInputStream() != null) {
                in = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
                br = new BufferedReader(in);
                String line = br.readLine();
                while (line != null) {
                    sb.append(line).append("\n");
                    line = br.readLine();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Throwable ignored) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable ignored) {
                }
            }
        }
        return sb.toString();
    }

}