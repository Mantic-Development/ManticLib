package me.fullpage.manticlib;

import lombok.Getter;
import me.fullpage.manticlib.command.ManticCommand;
import me.fullpage.manticlib.listeners.ArmourListener;
import me.fullpage.manticlib.listeners.DispenserArmorListener;
import me.fullpage.manticlib.listeners.PlayerMoveListener;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

@Getter
public class ManticPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        ArmourListener armourListener = new ArmourListener();
        try {
            Class.forName("org.bukkit.event.block.BlockDispenseArmorEvent");
            DispenserArmorListener dispenserArmorListener = new DispenserArmorListener();
        } catch (Exception e) {
            // ignore
        }

        PlayerMoveListener playerMoveListener = new PlayerMoveListener();
    }


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
