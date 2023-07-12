package me.fullpage.manticlib.command.impl;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.fullpage.manticlib.ManticLib;
import me.fullpage.manticlib.builders.Page;
import me.fullpage.manticlib.command.ManticCommand;
import me.fullpage.manticlib.command.TabCompleteBuilder;
import me.fullpage.manticlib.command.TabCompleteElement;
import me.fullpage.manticlib.integrations.manager.Integration;
import me.fullpage.manticlib.string.Txt;
import me.fullpage.manticlib.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ManticLibCmd extends ManticCommand {

    private static final String[] subCommands = {"integrations", "version", "nbtinfo"};


    public ManticLibCmd() {
        super("manticlib", "manticlib.command");
        Permission permission = new Permission("manticlib.command", "ManticLib command permission", PermissionDefault.OP);
        Bukkit.getServer().getPluginManager().addPermission(permission);
        this.setUsage("/<command> <" + String.join("/", subCommands) + "> [page]");
        this.setMinimumArgs(1);

    }

    @Override
    public void run() {

        if (args[0].equalsIgnoreCase("integrations")) {


            this.sendMessage("§7 ");
            Page<Integration> page = new Page<>(Txt.parse("&a&lACTIVE &8&l| &b&lManticLib Integrations &7{page}/{max_page}"), Integration.INTEGRATIONS.stream().filter(new Predicate<Integration>() {
                @Override
                public boolean test(Integration integration) {
                    return integration.isActive() && !integration.getProvidingPlugin().getName().equals("ManticLib");
                }
            }).collect(Collectors.toList()), (entry, index) -> Txt.parse("&d{0}: &7&l{1}&r&7 into &7&l{2}", index + 1, entry.getProvidingPlugin().getName(), entry.getPluginName()));

            String input = getArg(1).orElse("1");
            page.send(sender, Utils.isInt(input) ? Integer.parseInt(input) : 1);
            return;
        } else if (args[0].equalsIgnoreCase("ver") || args[0].equalsIgnoreCase("version")) {
            this.sendMessage("&7ManticLib version: " + ManticLib.get().getDescription().getVersion());
            return;
        } else if (args[0].equalsIgnoreCase("nbtinfo")) {
            if (isConsole()) {
                return;
            }
            this.sendMessage("&7--- &nNBT INFO&r&7 ---" );
            try {
                NBTItem nbtItem = new NBTItem(player.getItemInHand());
                sender.sendMessage("&7NBT: " + nbtItem.toString());
            } catch (Throwable e) {
                this.sendMessage("&4Error: &c" + e.getMessage());
            }
            return;
        }

        sendUsageMessage();
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return TabCompleteBuilder.create(args, sender)
                .add(new TabCompleteElement(0, subCommands))
                .build();
    }
}
