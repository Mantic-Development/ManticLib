package me.fullpage.manticlib;

import com.google.common.annotations.Beta;
import lombok.Getter;
import me.fullpage.manticlib.command.ManticCommand;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Set;

@Getter
public class ManticPlugin extends JavaPlugin {


    /**
     * Use ManticPlugin#onInnerDisable() instead.
     */
    @Override
    public void onDisable() {
        super.onDisable();

        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);
        Set<ManticCommand> toClear = ManticCommand.getCommands(this);
        for (ManticCommand command : toClear) {
            if (command != null) {
                toClear.add(command);
            }
        }
        toClear.forEach(ManticCommand::unregister);
        toClear.clear();

        onInnerDisable();
    }

    public void onInnerDisable() {
    }

    private static final HashMap<String, String> providingPluginData = new HashMap<>();

    @Beta
    @NotNull
    public static JavaPlugin getProvidingPlugin(@NotNull Class<?> clazz) {
        JavaPlugin plugin = null;
        Exception exception = null;
        try {
            plugin = JavaPlugin.getProvidingPlugin(clazz);
            if (!providingPluginData.containsKey(clazz.getName())) {
                providingPluginData.put(clazz.getName(), plugin.getName());
            }
        } catch (IllegalStateException e) {
            exception = e;
            try {
                String name = providingPluginData.get(clazz.getName());
                if (name != null) {
                    plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(name);
                }
            } catch (Exception ex) {
                exception = ex;
               plugin = null;
            }
        }
        if (plugin == null) {
            throw new IllegalStateException("\033[1;31mPlease do not use plugins like \"Plugman\" to load or unload a plugin during runtime. Instead use built-in reload commands in plugins or restart where possible.", exception);
        }
        return plugin;
    }


}
