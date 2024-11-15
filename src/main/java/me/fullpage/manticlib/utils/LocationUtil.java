package me.fullpage.manticlib.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Objects;

public class LocationUtil {

    public static boolean isBlockAboveAir(Block block) {
        return block.getRelative(BlockFace.DOWN).getType() == Material.AIR;
    }

    public static double distance(Location loc, Location pLoc) {
        return Math.sqrt(Math.pow(pLoc.getX() - loc.getX(), 2) +
                Math.pow(pLoc.getY() - loc.getY(), 2) +
                Math.pow(pLoc.getZ() - loc.getZ(), 2));
    }

    public static Location getHandLocation(Player player) {
        Location loc = player.getLocation().clone();

        double a = loc.getYaw() / 180D * Math.PI + Math.PI / 2;
        double l = Math.sqrt(0.8D * 0.8D + 0.4D * 0.4D);

        loc.setX(loc.getX() + l * Math.cos(a) - 0.8D * Math.sin(a));
        loc.setY(loc.getY() + player.getEyeHeight() - 0.2D);
        loc.setZ(loc.getZ() + l * Math.sin(a) + 0.8D * Math.cos(a));

        return loc;
    }

    public static boolean inRegion(Location playerLoc, Location point1, Location point2) {
        if (playerLoc == null || point1 == null  || point2 == null) {
            return false;
        }
        if (Objects.requireNonNull(point1.getWorld()).getName().equalsIgnoreCase(Objects.requireNonNull(playerLoc.getWorld()).getName())
                && Objects.requireNonNull(point2.getWorld()).getName().equalsIgnoreCase(playerLoc.getWorld().getName())) {
            double highestX = Math.max(point1.getX(), point2.getX()), highestY = Math.max(point1.getY(), point2.getY()), highestZ = Math.max(point1.getZ(), point2.getZ()),
                    lowestX = Math.min(point1.getX(), point2.getX()), lowestY = Math.min(point1.getY(), point2.getY()), lowestZ = Math.min(point1.getZ(), point2.getZ());
            return (highestX >= playerLoc.getBlockX() && lowestX <= playerLoc.getBlockX()) && (highestY >= playerLoc.getBlockY()
                    && lowestY <= playerLoc.getBlockY()) && (highestZ >= playerLoc.getBlockZ() && lowestZ <= playerLoc.getBlockZ());
        }
        return false;
    }

}
