package me.fullpage.manticlib.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class PlayerChunkMoveEvent extends ManticPlayerEvent {


    private static final HandlerList handlerList = new HandlerList();
    private final Chunk to;
    private final Chunk from;
    private final Location toLocation;
    private final Location fromLocation;

    public PlayerChunkMoveEvent(Player player, Chunk to, Chunk from, Location toLocation, Location fromLocation) {
        super(player);
        this.to = to;
        this.from = from;
        this.toLocation = toLocation;
        this.fromLocation = fromLocation;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}
