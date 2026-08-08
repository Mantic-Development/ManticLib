package me.fullpage.manticlib.listeners;

import me.fullpage.manticlib.ManticLib;
import me.fullpage.manticlib.utils.Utils;
import me.fullpage.manticlib.wrappers.ForwardingData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandForwarder implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {

        List<ForwardingData> forwardingDataList = ManticLib.get().getConfiguration().forwardingData;

        if (Utils.isNullOrEmpty(forwardingDataList)) {
            return;
        }

        String raw = stripLeadingSlash(event.getMessage());
        if (raw.isEmpty()) return;

        String[] inputTokens = raw.trim().split("\\s+");

        String bestTarget = null;
        int bestAliasLength = -1;
        int bestArgsStart = -1;

        for (ForwardingData data : forwardingDataList) {
            String target = stripLeadingSlash(data.getTarget());
            if (target.isEmpty()) {
                continue;
            }

            List<String> aliases = data.getAliases();
            if (aliases == null || aliases.isEmpty()) {
                continue;
            }

            for (String alias : aliases) {
                String cleanAlias = stripLeadingSlash(alias).trim();
                if (cleanAlias.isEmpty()) continue;

                String[] aliasTokens = cleanAlias.split("\\s+");
                boolean matched = matches(inputTokens, aliasTokens);

                if (matched && aliasTokens.length > bestAliasLength) {
                    bestAliasLength = aliasTokens.length;
                    bestTarget = target;
                    bestArgsStart = aliasTokens.length;
                }
            }
        }

        if (bestTarget == null) {
            return;
        }

        StringBuilder newCommand = new StringBuilder(bestTarget);
        for (int i = bestArgsStart; i < inputTokens.length; i++) {
            newCommand.append(" ").append(inputTokens[i]);
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        boolean success = player.performCommand(newCommand.toString());
    }

    private boolean matches(String[] inputTokens, String[] aliasTokens) {
        if (inputTokens.length < aliasTokens.length) return false;

        for (int i = 0; i < aliasTokens.length; i++) {
            if (!inputTokens[i].equalsIgnoreCase(aliasTokens[i])) {
                return false;
            }
        }
        return true;
    }

    private String stripLeadingSlash(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("/")) {
            s = s.substring(1);
        }
        return s;
    }
}