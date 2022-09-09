package me.fullpage.manticlib.command;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Getter
public class TabCompleteElement {

    private final int index;
    private final String[] results;
    private String permission;

    public TabCompleteElement(int index, String... results) {
        this.index = index;
        this.results = results;
    }

    public TabCompleteElement setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public boolean hasPermission(CommandSender sender) {
        return this.permission == null || sender == null || sender.hasPermission(this.permission);
    }

    public static TabCompleteElement getOnlinePlayers(int index, CommandSender sender) {
        if (sender instanceof Player) {
            return new TabCompleteElement(index, Bukkit.getOnlinePlayers().stream().filter(s -> ((Player) sender).canSee(s)).map(HumanEntity::getName).toArray(String[]::new));
        }
        return new TabCompleteElement(index, Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toArray(String[]::new));
    }

    public static TabCompleteElement getAllPlayers(int index, CommandSender sender) {
        if (sender instanceof Player) {
            return new TabCompleteElement(index, Arrays.stream(Bukkit.getOfflinePlayers()).filter(s -> {
                if (s instanceof Player) {
                    return ((Player) sender).canSee((Player) s);
                }
                return true;
            }).map(OfflinePlayer::getName).toArray(String[]::new));
        }
        return new TabCompleteElement(index, Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toArray(String[]::new));
    }



}
