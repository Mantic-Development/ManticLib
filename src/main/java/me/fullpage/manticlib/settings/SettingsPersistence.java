package me.fullpage.manticlib.settings;

import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

final class SettingsPersistence {
    private static final Map<Plugin, SettingsPersistence> WRITERS = new WeakHashMap<>();
    private final ThreadPoolExecutor executor;
    private final Logger logger;
    private volatile Thread worker;

    private SettingsPersistence(Plugin plugin) {
        logger = plugin.getLogger();
        String name = plugin.getName() + "-settings-writer";
        executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(256), task -> {
            Thread thread = new Thread(task, name);
            thread.setDaemon(true);
            worker = thread;
            return thread;
        });
        executor.allowCoreThreadTimeOut(true);
    }

    static synchronized SettingsPersistence forPlugin(Plugin plugin) {
        SettingsPersistence writer = WRITERS.get(plugin);
        if (writer == null || writer.executor.isTerminated()) {
            writer = new SettingsPersistence(plugin);
            WRITERS.put(plugin, writer);
        }
        return writer;
    }

    synchronized CompletableFuture<Void> submit(Path destination, Supplier<String> capture) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        try {
            if (Thread.currentThread() == worker) {
                throw new RejectedExecutionException("Do not save from a writer completion callback; schedule on the state-owning thread");
            }
            if (executor.isShutdown()) throw new RejectedExecutionException("Settings writer is closed");
            final String json = capture.get();
            executor.execute(() -> {
                try {
                    replace(destination, json);
                    result.complete(null);
                } catch (Exception e) {
                    fail(destination, result, e);
                }
            });
        } catch (Exception e) {
            fail(destination, result, e);
        }
        return result;
    }

    private void fail(Path destination, CompletableFuture<Void> result, Exception error) {
        logger.log(Level.SEVERE, "Could not save settings to " + destination, error);
        result.completeExceptionally(error);
    }

    boolean shutdown(long timeout, TimeUnit unit) throws InterruptedException {
        if (timeout < 0) throw new IllegalArgumentException("timeout must not be negative");
        if (unit == null) throw new NullPointerException("unit");
        synchronized (this) {
            executor.shutdown();
        }
        if (Thread.currentThread() == worker) return false;
        boolean finished = executor.awaitTermination(timeout, unit);
        if (!finished) logger.severe("Timed out waiting for settings writes; queued writes will continue in order");
        return finished;
    }

    static void replace(Path destination, String json) throws IOException {
        replace(destination, json, Files::move);
    }

    @FunctionalInterface
    interface Move {
        Path move(Path source, Path target, CopyOption... options) throws IOException;
    }

    static void replace(Path destination, String json, Move move) throws IOException {
        Path target = destination.toFile().getCanonicalFile().toPath();
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), ".settings-", ".tmp");
        try {
            Files.write(temporary, json.getBytes(StandardCharsets.UTF_8));
            try {
                move.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                // A recoverable backup is required before attempting a replacement.
                Path backup = null;
                if (Files.exists(target)) {
                    backup = Files.createTempFile(target.getParent(), ".settings-backup-", ".json");
                    Files.copy(target, backup, StandardCopyOption.REPLACE_EXISTING);
                }
                try {
                    move.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException failure) {
                    if (backup != null) {
                        try {
                            Files.copy(backup, target, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException restoreFailure) {
                            failure.addSuppressed(restoreFailure);
                        }
                        throw new IOException("Replacement failed; previous save retained at " + backup, failure);
                    }
                    throw failure;
                }
                if (backup != null) Files.deleteIfExists(backup);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
