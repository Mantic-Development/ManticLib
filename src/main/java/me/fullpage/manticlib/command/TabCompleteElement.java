package me.fullpage.manticlib.command;

import lombok.Getter;
import me.fullpage.manticlib.command.impl.OnlinePlayersTabCompleteElement;
import me.fullpage.manticlib.interfaces.Permission;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Unmodifiable;

@Getter
public class TabCompleteElement {

    @Unmodifiable
    private static final String[] EMPTY = new String[0];

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

    public TabCompleteElement(int index) {
        this.index = index;
        this.results = EMPTY;
        this.permission = null;
        this.conditions = new Condition[0];
    }

    public TabCompleteElement setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public TabCompleteElement setPermission(Permission permission) {
        return this.setPermission(permission.getPermission());
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
// TODO check if player is vanished effectively
        return new OnlinePlayersTabCompleteElement(index, sender);
    }

    /**
     * @apiNote This method is deprecated due to inefficiency. Use {@link #getOnlinePlayers(int, CommandSender)} instead.
     */
    @Deprecated
    public static TabCompleteElement getAllPlayers(int index, CommandSender sender) {
        return new TabCompleteElement(index, EMPTY);
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
