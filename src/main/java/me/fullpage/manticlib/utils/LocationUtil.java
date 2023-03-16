package me.fullpage.manticlib.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

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

}
