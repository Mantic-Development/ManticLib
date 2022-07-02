package me.fullpage.manticlib.integrations.manager;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class Integration implements Listener {

    private final @NotNull String pluginName;
    private List<String> requiredClasses = new ArrayList<>();
    private List<String> requiredVersions = new ArrayList<>();
    private JavaPlugin providingPlugin;
    private boolean active;

    public Integration(@NotNull String pluginName) {
        this.pluginName = pluginName;
        this.active = false;
        providingPlugin = JavaPlugin.getProvidingPlugin(this.getClass());
        Bukkit.getServer().getPluginManager().registerEvents(this, providingPlugin);
        this.checkActive();
    }

    public void addRequiredClass(@NotNull String classPath) {
        this.requiredClasses.add(classPath);
    }

    public void addRequiredVersion(@NotNull String version) {
        this.requiredVersions.add(version);
    }

    public void onEnable() {

    }

    public void onDisable() {

    }

    private void checkActive() {
        Bukkit.getScheduler().runTaskLater(providingPlugin, ()->{

            if (check()) {
                if (!active) {
                    providingPlugin.getLogger().info("Enabled integration for " + pluginName + ".");
                    this.onEnable();
                }
                active = true;
            } else {
                if (active) {
                    providingPlugin.getLogger().info("Disabled integration for " + pluginName + ".");
                    this.onDisable();
                }
                active = false;
            }
        }, 2L);
    }

    private boolean check() {
        if (!isPlugin()) {
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

    private boolean isPlugin() {
        if (!Bukkit.getServer().getPluginManager().isPluginEnabled(this.pluginName)) {
            return false;
        }

        if (requiredVersions != null && !requiredVersions.isEmpty()) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(this.pluginName);
            for (String requiredVersion : requiredVersions) {
                if (requiredVersion != null && plugin != null && !plugin.getDescription().getVersion().equals(requiredVersion)) {
                    return true;
                }
            }
        } else {
            return true;
        }

        return false;
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
