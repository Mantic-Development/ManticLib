package me.fullpage.manticlib.events;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ManticNBTItemInteractEvent extends ManticPlayerEvent {


    private static final HandlerList handlerList = new HandlerList();

    @NotNull
    private final NBTItem nbtItem;
    private final PlayerInteractEvent playerInteractEvent;

    public ManticNBTItemInteractEvent(Player player, @NotNull NBTItem nbtItem,PlayerInteractEvent playerInteractEvent) {
        super(player);
        this.nbtItem = nbtItem;
        this.playerInteractEvent = playerInteractEvent;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    public NBTItem getNbtItem() {
        return nbtItem;
    }

    public ItemStack getItemStack() {
        return nbtItem.getItem();
    }

    public PlayerInteractEvent getPlayerInteractEvent() {
        return playerInteractEvent;
    }
}
