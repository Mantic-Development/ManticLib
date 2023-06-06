package me.fullpage.manticlib.player;

import lombok.NonNull;
import me.fullpage.manticlib.string.Txt;
import me.fullpage.manticlib.utils.ActionBar;
import me.fullpage.manticlib.utils.TitleAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

public class ManticPlayer {

    private transient @Nullable OfflinePlayer player;
    private final @NonNull UUID uniqueId;

    public ManticPlayer(@NonNull UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public ManticPlayer(@NonNull Player player) {
        this.player = player;
        this.uniqueId = player.getUniqueId();
    }

    public @Nullable Player getPlayer() {
        return player instanceof Player ? (Player) player : (Player) (player = Bukkit.getPlayer(uniqueId));
    }

    public @NonNull OfflinePlayer getOfflinePlayer() {
        return player != null ? player : (player = Bukkit.getOfflinePlayer(uniqueId));
    }

    public @NonNull UUID getUniqueId() {
        return uniqueId;
    }

    public ManticPlayer msg(String message) {
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage(Txt.parse(message));
        }
        return this;
    }

    public ManticPlayer msg(String message, Object... args) {
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage(Txt.parse(message, args));
        }
        return this;
    }

    public ManticPlayer sendMessage(String message) {
        return msg(message);
    }

    public ManticPlayer sendMessage(String message, Object... args) {
        return msg(message, args);
    }

    public ManticPlayer sendActionBar(String message) {
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            ActionBar.sendActionBar(player, Txt.parse(message));
        }
        return this;
    }

    public ManticPlayer sendActionBar(String message, Object... args) {
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            ActionBar.sendActionBar(player, Txt.parse(message, args));
        }
        return this;
    }

    public ManticPlayer sendTitle(String title, String subtitle) {
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            TitleAPI.sendTitle(player, 20, 20, 20, Txt.parse(title), Txt.parse(subtitle));
        }
        return this;
    }

    public ManticPlayer sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            TitleAPI.sendTitle(player, fadeIn, stay, fadeOut, Txt.parse(title), Txt.parse(subtitle));
        }
        return this;
    }

    public ManticPlayer clearTitle() {
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            TitleAPI.clearTitle(player);
        }
        return this;
    }



}
