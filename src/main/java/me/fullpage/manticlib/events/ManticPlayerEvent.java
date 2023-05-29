package me.fullpage.manticlib.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
 
public abstract class ManticPlayerEvent extends PlayerEvent implements Runnable, Cancellable {

    private boolean cancelled = false;

    public ManticPlayerEvent(@NotNull Player who) {
        super(who);
    }

    @Override
    public void run() {
        Bukkit.getPluginManager().callEvent(this);
    }

    public void call() {
        this.run();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
    
}
