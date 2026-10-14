package me.fullpage.manticlib.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.logging.Level;

public final class MainThreadExecutor implements Executor {

    private final Plugin plugin;
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private volatile BukkitTask task;

    public MainThreadExecutor(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void start() {
        if (task != null) {
            return;
        }
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::drain, 1L, 1L);
    }

    @Override
    public void execute(Runnable command) {
        queue.add(Objects.requireNonNull(command, "command"));
    }

    public void close() {
        BukkitTask current = task;
        task = null;
        if (current != null) {
            current.cancel();
        }
        if (Bukkit.isPrimaryThread()) {
            drain();
        }
    }

    private void drain() {
        // snapshot so producers cannot starve the tick
        for (int remaining = queue.size(); remaining > 0; remaining--) {
            Runnable next = queue.poll();
            if (next == null) {
                return;
            }
            try {
                next.run();
            } catch (Throwable t) {
                plugin.getLogger().log(Level.SEVERE, "Main thread task failed", t);
            }
        }
    }
}
