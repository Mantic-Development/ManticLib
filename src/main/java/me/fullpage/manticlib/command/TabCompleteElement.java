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
    private Condition[] conditions;
    private String permission;

    public TabCompleteElement(int index, String... results) {
        this.index = index;
        this.results = results;
        this.permission = null;
        this.conditions = new Condition[0];
    }

    public TabCompleteElement setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public boolean hasPermission(CommandSender sender) {
        return this.permission == null || sender == null || sender.hasPermission(this.permission);
    }

    public TabCompleteElement addCondition(Condition condition) {
        Condition[] newConditions = new Condition[this.conditions.length + 1];
        System.arraycopy(this.conditions, 0, newConditions, 0, this.conditions.length);
        newConditions[this.conditions.length] = condition;
        this.conditions = newConditions;
        return this;
    }

    public boolean meetsConditions(String[] args) {
        if (this.conditions.length == 0) return true;
        for (Condition condition : this.conditions) {
            if (condition.meets(args)) {
                return true;
            }
        }
        return false;
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

    @Getter
    public static class Condition {

        private final int index;
        private final boolean ignoreCase;
        private final String[] matches;
        private boolean invert = false;

        public Condition(int index, boolean ignoreCase, String... matches) {
            this.index = index;
            this.ignoreCase = ignoreCase;
            this.matches = matches;
        }

        public Condition invert() {
            this.invert = !this.invert;
            return this;
        }

        public boolean meets(String[] args) {
            if (args.length <= this.index) {
                return false;
            }
            for (String match : this.matches) {
                if (this.ignoreCase) {
                    if (args[this.index].equalsIgnoreCase(match)) {
                        return !this.invert;
                    }
                } else {
                    if (args[this.index].equals(match)) {
                        return !this.invert;
                    }
                }
            }
            return this.invert;
        }



    }


}
