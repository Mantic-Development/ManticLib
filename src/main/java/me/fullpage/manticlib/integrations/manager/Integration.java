package me.fullpage.manticlib.integrations.manager;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class Integration {

    public static List<Integration> INTEGRATIONS = new ArrayList<>();
    private static boolean isInitialized = false;

    private final @NotNull String pluginName;
    private List<String> requiredClasses = new ArrayList<>();
    private List<String> requiredVersions = new ArrayList<>();
    protected JavaPlugin providingPlugin;
    private boolean active;

    public Integration(@NotNull String pluginName) {
        this.pluginName = pluginName;
        this.active = false;
        providingPlugin = JavaPlugin.getProvidingPlugin(this.getClass());
        this.setActive();
        INTEGRATIONS.add(this);
        if (!isInitialized) {
            isInitialized = true;
            Bukkit.getServer().getPluginManager().registerEvents(new IntegrationListener(), providingPlugin);
        }
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

    protected void setActive() {
        this.setActive(null);
    }

    protected void setActive(Boolean active) {

        if (active == null) {
            active = this.check();
        }

        if (active == this.active) {
            return;
        }

        if (active) {
            this.active = true;
            providingPlugin.getLogger().info("Enabled integration for " + pluginName + ".");
            this.onEnable();
        } else {
            this.active = false;
            providingPlugin.getLogger().info("Disabled integration for " + pluginName + ".");
            this.onDisable();
        }

       /* if (check()) {
            if (!this.active) {
                providingPlugin.getLogger().info("Enabled integration for " + pluginName + ".");
                this.onEnable();
            }
            this.active = true;
        } else {
            this.forceDisable();
        }*/
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

    public void forceDisable() {
        if (active) {
            providingPlugin.getLogger().info("Disabled integration for " + pluginName + ".");
            this.onDisable();
        }
        active = false;
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
                if (requiredVersion != null && plugin != null && plugin.getDescription().getVersion().equals(requiredVersion)) {
                    return true;
                }
            }
        } else {
            return true;
        }

        return false;
    }

}
