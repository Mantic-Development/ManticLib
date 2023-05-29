package me.fullpage.manticlib.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

// Inspired by MassiveCore
public abstract class ManticEvent extends Event implements Runnable, Cancellable {

    private boolean cancelled = false;

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
