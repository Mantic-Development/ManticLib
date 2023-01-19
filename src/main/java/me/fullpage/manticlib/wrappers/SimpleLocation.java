package me.fullpage.manticlib.wrappers;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.fullpage.manticlib.utils.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode
public class SimpleLocation implements Serializable {

    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float pitch;
    private final float yaw;

    public SimpleLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public SimpleLocation(String worldName, double x, double y, double z) {
        this(worldName, x, y, z, 0f, 0f);
    }

    public SimpleLocation(@NonNull Location location) {
        this(Objects.requireNonNull(location.getWorld(), "world cannot be null").getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public Location asLocation() {
        final World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    public SimpleChunk asSimpleChunk() {
        final int[] ints = MathUtils.chunkFromBlock(((Double) x).intValue(), ((Double) z).intValue());
        return new SimpleChunk(worldName, ints[0], ints[1]);
    }

}
