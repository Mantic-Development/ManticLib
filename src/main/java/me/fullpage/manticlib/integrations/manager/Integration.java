package me.fullpage.manticlib.integrations.manager;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class Integration implements Listener {

    private final @NotNull String pluginName;
    private List<String> requiredClasses = new ArrayList<>();
    private JavaPlugin providingPlugin;
    private boolean active;

    public Integration(@NotNull String pluginName) {
        this.pluginName = pluginName;
        this.active = false;
        providingPlugin = JavaPlugin.getProvidingPlugin(this.getClass());
        Bukkit.getServer().getPluginManager().registerEvents(this, providingPlugin);
        this.checkActive();
    }

    public void addRequiredClass(String classPath) {
        this.requiredClasses.add(classPath);
    }

    private void checkActive() {
        if (check()) {
            if (!active) {
                providingPlugin.getLogger().info("Enabled integration for " + pluginName + ".");
            }
            active = true;
        } else {
            if (active) {
                providingPlugin.getLogger().info("Disabled integration for " + pluginName + ".");
            }
            active = false;
        }
    }

    private boolean check() {
        if (!isPluginOn()) {
            return false;
        }

        if (requiredClasses != null && !requiredClasses.isEmpty()) {
            for (String requiredClass : requiredClasses) {
                if (!isClassLoaded(requiredClass)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isClassLoaded(String classPath) {
        if (classPath == null || classPath.isEmpty()) return false;
        try {
            Class.forName(classPath);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean isPluginOn() {
        return Bukkit.getServer().getPluginManager().isPluginEnabled(this.pluginName);
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginEnable(PluginEnableEvent event) {
        this.checkActive();
    }
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginDisable(PluginDisableEvent event) {
        this.checkActive();
    }

}
