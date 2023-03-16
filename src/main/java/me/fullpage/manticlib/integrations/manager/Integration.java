package me.fullpage.manticlib.integrations.manager;

import lombok.Getter;
import lombok.Setter;
import me.fullpage.manticlib.Versionator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Getter
@Setter
public abstract class Integration {

    public static List<Integration> INTEGRATIONS = new ArrayList<>();
    private static boolean isInitialized = false;

    private final @NotNull String pluginName;
    private List<String> requiredClasses = new ArrayList<>();
    private List<String> requiredVersions = new ArrayList<>();
    private String minimumVersion = null;
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
            if (!check()) {
                return;
            }
            this.active = true;
            providingPlugin.getLogger().info("Enabled integration for " + pluginName + ".");
            try {
                this.onEnable();
            } catch (Exception e) {
                this.setActive(false);
                providingPlugin.getLogger().log(Level.SEVERE, "Failed to enable integration for " + pluginName + ".", e);
            }
        } else {
            this.active = false;
            providingPlugin.getLogger().info("Disabled integration for " + pluginName + ".");
            try {
                this.onDisable();
            } catch (Exception e) {
                providingPlugin.getLogger().log(Level.SEVERE, "An error occurred while disabling integration for " + pluginName + ".", e);
            }
        }

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

        Plugin plugin = null;

        if (minimumVersion != null) {
            plugin = Bukkit.getServer().getPluginManager().getPlugin(this.pluginName);
            if (plugin == null) {
                return false;
            }

            if (!this.isAtLeastMinimumVersion(plugin)) {
                return false;
            }

        }


        if (requiredVersions != null && !requiredVersions.isEmpty()) {
            if (plugin == null) plugin = Bukkit.getPluginManager().getPlugin(this.pluginName);
            for (String requiredVersion : requiredVersions) {
                if (requiredVersion != null && plugin != null && plugin.getDescription().getVersion().equals(requiredVersion)) {
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    public boolean isAtLeastMinimumVersion(@NotNull Plugin plugin) {
        if (minimumVersion == null) return true;
        return Versionator.convertVersion(plugin.getDescription().getVersion()) >= Versionator.convertVersion(minimumVersion);
    }

    /**
     * the plugin version must contain numbers for this to work
     */
    public void setMinimumVersion(String minimumVersion) {
        this.minimumVersion = minimumVersion;
    }

}
