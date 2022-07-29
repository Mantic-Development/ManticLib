package me.fullpage.manticlib.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class PlayerChunkMoveEvent extends Event implements Cancellable {


    private static final HandlerList handlerList = new HandlerList();
    private boolean isCancelled;
    private final Player player;
    private final Chunk to;
    private final Chunk from;
    private final Location toLocation;
    private final Location fromLocation;

    public PlayerChunkMoveEvent(Player player, Chunk to, Chunk from, Location toLocation, Location fromLocation) {
        this.player = player;
        this.to = to;
        this.from = from;
        this.toLocation = toLocation;
        this.fromLocation = fromLocation;
        isCancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}
