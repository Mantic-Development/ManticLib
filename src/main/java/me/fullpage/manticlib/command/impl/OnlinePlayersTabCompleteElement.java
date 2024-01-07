package me.fullpage.manticlib.command.impl;

import me.fullpage.manticlib.command.TabCompleteElement;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class OnlinePlayersTabCompleteElement extends TabCompleteElement {

    private CommandSender sender;


    public OnlinePlayersTabCompleteElement(int index, CommandSender sender) {
        super(index);
        this.sender = sender;
    }

    public List<String> getResults(String arg) {
        List<String> results = new ArrayList<>();
        int count = 0;
        for (CommandSender player : sender.getServer().getOnlinePlayers()) {
            String name = player.getName();
            if (arg != null && name.toLowerCase().startsWith(arg)) {
                results.add(player.getName());
                count++;
                if (count >= 100) break;
            }
        }
        return results;

    }

    /* TODO public void cache() {

    }

    public void clearCache() {

    }*/

}
