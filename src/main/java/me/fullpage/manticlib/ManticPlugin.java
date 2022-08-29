package me.fullpage.manticlib;

import lombok.Getter;
import me.fullpage.manticlib.command.ManticCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

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


}
