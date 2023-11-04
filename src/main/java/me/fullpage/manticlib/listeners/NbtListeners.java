package me.fullpage.manticlib.listeners;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.fullpage.manticlib.events.ManticNBTItemInteractEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class NbtListeners implements Listener {


    // TODO: Complete

    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR || item.getAmount() < 1) {
            return;
        }

        NBTItem nbtItem = new NBTItem(item);
        ManticNBTItemInteractEvent manticNBTItemInteractEvent = new ManticNBTItemInteractEvent(event.getPlayer(), nbtItem, event);
        manticNBTItemInteractEvent.call();
        event.setCancelled(manticNBTItemInteractEvent.isCancelled());


    }

}
