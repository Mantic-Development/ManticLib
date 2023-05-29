package me.fullpage.manticlib.utils;

import me.fullpage.manticlib.ManticLib;
import me.fullpage.nmslib.plugin.NMSLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBar {

    public static void sendActionBar(Player player, String message) {
        if (!player.isOnline()) {
            return;
        }

        NMSLib.getNmsHandler().sendActionBar(player, message);
    }

    public static void sendActionBar(final Player player, final String message, int duration) {
        sendActionBar(player, message);

        if (duration >= 0) {
            // Sends empty message at the end of the duration. Allows messages shorter than 3 seconds, ensures precision.
            new BukkitRunnable() {
                @Override
                public void run() {
                    sendActionBar(player, "");
                }
            }.runTaskLater(ManticLib.get(), duration + 1);
        }

        // Re-sends the messages every 3 seconds so it doesn't go away from the player's screen.
        while (duration > 40) {
            duration -= 40;
            new BukkitRunnable() {
                @Override
                public void run() {
                    sendActionBar(player, message);
                }
            }.runTaskLater(ManticLib.get(), duration);
        }
    }

    public static void sendActionBarToAllPlayers(String message) {
        sendActionBarToAllPlayers(message, -1);
    }

    public static void sendActionBarToAllPlayers(String message, int duration) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            sendActionBar(p, message, duration);
        }
    }
}
