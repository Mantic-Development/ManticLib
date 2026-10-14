package me.fullpage.manticlib.listeners;

import me.fullpage.manticlib.ManticLib;
import me.fullpage.manticlib.events.PlayerChunkMoveEvent;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private static boolean REGISTERED = false;


    public PlayerMoveListener() {
        if (!REGISTERED) {
            ManticLib.get().getServer().getPluginManager().registerEvents(this, ManticLib.get());
            REGISTERED = true;
        }
    }

    // Runs for every player move, so stay allocation-free until a chunk border is crossed.
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        final Location to = event.getTo();
        if (to == null) {
            return;
        }
        final Location from = event.getFrom();
        final World world = from.getWorld();
        if (world == null) {
            return;
        }
        final int toChunkX = to.getBlockX() >> 4;
        final int toChunkZ = to.getBlockZ() >> 4;
        final int fromChunkX = from.getBlockX() >> 4;
        final int fromChunkZ = from.getBlockZ() >> 4;
        if (toChunkX == fromChunkX && toChunkZ == fromChunkZ) {
            return;
        }

        Chunk toFChunk = world.getChunkAt(toChunkX, toChunkZ);
        Chunk fromFChunk = world.getChunkAt(fromChunkX, fromChunkZ);

        PlayerChunkMoveEvent playerChunkMoveEvent = new PlayerChunkMoveEvent(event.getPlayer(), toFChunk, fromFChunk, to, from);
        playerChunkMoveEvent.call();

        if (playerChunkMoveEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

}
