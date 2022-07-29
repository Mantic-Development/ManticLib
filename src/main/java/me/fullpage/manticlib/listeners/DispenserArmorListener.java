package me.fullpage.manticlib.listeners;

import me.fullpage.manticlib.ManticLib;
import me.fullpage.manticlib.events.armourequipevent.ArmourEquipEvent;
import me.fullpage.manticlib.events.armourequipevent.ArmourType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;

public class DispenserArmorListener implements Listener {
    private static boolean REGISTERED = false;

    public DispenserArmorListener() {
        if (!REGISTERED) {
            ManticLib.get().getServer().getPluginManager().registerEvents(this, ManticLib.get());
            REGISTERED = true;
        }
    }


    @EventHandler
    public void dispenseArmorEvent(BlockDispenseArmorEvent event) {
        ArmourType type = ArmourType.matchType(event.getItem());
        if (type != null) {
            if (event.getTargetEntity() instanceof Player) {
                Player p = (Player) event.getTargetEntity();
                ArmourEquipEvent armorEquipEvent = new ArmourEquipEvent(p, ArmourEquipEvent.EquipMethod.DISPENSER, type, null, event.getItem());
                ManticLib.get().getServer().getPluginManager().callEvent(armorEquipEvent);
                if (armorEquipEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }
    }

}