package me.fullpage.manticlib.integrations.manager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import java.util.ArrayList;
import java.util.List;

public class IntegrationListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginEnable(PluginEnableEvent event) {
        for (Integration integration : Integration.INTEGRATIONS) {
            if (integration != null && integration.getPluginName().equalsIgnoreCase(event.getPlugin().getName())) {
                integration.checkActive();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginDisable(PluginDisableEvent event) {
        List<Integration> toRemove = new ArrayList<>();
        for (Integration integration : Integration.INTEGRATIONS) {
            if (integration == null) {
                continue;
            }
            if (integration.providingPlugin != null && !integration.providingPlugin.isEnabled()) {
                toRemove.add(integration);
            }
            if (integration.getPluginName().equalsIgnoreCase(event.getPlugin().getName())) {
                integration.checkActive();
            }
        }
        if (!toRemove.isEmpty()) {
            Integration.INTEGRATIONS.removeAll(toRemove);
        }
    }

}
