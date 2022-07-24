package me.fullpage.manticlib.listeners;

import me.fullpage.manticlib.ManticLib;
import me.fullpage.manticlib.events.PlayerChunkMoveEvent;
import me.fullpage.manticlib.utils.MathUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
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

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Location to = event.getTo();
        if (to == null) {
            return;
        }
        final Location from = event.getFrom();

        final World world = from.getWorld();
        if (world == null) {
            return;
        }
        final int[] toI = MathUtils.chunkFromBlock(to.getBlockX(), to.getBlockZ());
        final int[] fromI = MathUtils.chunkFromBlock(from.getBlockX(), from.getBlockZ());

        int toChunkX = toI[0];
        int toChunkZ = toI[1];
        int fromChunkX = fromI[0];
        int fromChunkZ = fromI[1];

        if (toChunkX == fromChunkX && toChunkZ == fromChunkZ) {
            return;
        }

        final Chunk toFChunk = world.getChunkAt(toChunkX, toChunkZ);
        final Chunk fromFChunk = world.getChunkAt(fromChunkX, fromChunkZ);

        final PlayerChunkMoveEvent playerChunkMoveEvent = new PlayerChunkMoveEvent(player, toFChunk, fromFChunk, to, from);

        ManticLib.get().getServer().getPluginManager().callEvent(playerChunkMoveEvent);

        if (playerChunkMoveEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

}
