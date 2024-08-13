package me.fullpage.manticlib.wrappers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.Objects;

@AllArgsConstructor
@Getter
public class SimpleChunk {

    private final String worldName;
    private final int x, z;

    public Chunk asChunk() {
        return Objects.requireNonNull(Bukkit.getWorld(worldName), "world cannot be null").getChunkAt(x, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleChunk)) {
            if (o instanceof Chunk) {
                return SimpleChunk.from((Chunk) o).equals(this);
            }
            return false;
        }
        SimpleChunk that = (SimpleChunk) o;
        return x == that.x && z == that.z && worldName.equals(that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, z);
    }

    public static SimpleChunk from(Chunk location) {
        return new SimpleChunk(location.getWorld().getName(), location.getX(), location.getZ());
    }

    public static SimpleChunk from(Location location) {
        return from(new SimpleLocation(location));
    }

    public static SimpleChunk from(SimpleLocation location) {
        return location.asSimpleChunk();
    }



}
