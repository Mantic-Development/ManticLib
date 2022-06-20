package me.fullpage.manticlib.wrappers;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Getter
@Setter
@EqualsAndHashCode
public class SoundEffect {

    private final Sound sound;
    private final float volume, pitch;

    public SoundEffect(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public SoundEffect(String sound, float volume, float pitch) {
        this(getSound(sound), volume, pitch);
    }

    public SoundEffect(Sound sound) {
        this(sound, 1.0f, 1.0f);
    }

    public SoundEffect(String sound) {
        this(getSound(sound));
    }


    public SoundEffect withSound(Sound sound) {
        return new SoundEffect(sound, volume, pitch);
    }

    public SoundEffect withVolume(float volume) {
        return new SoundEffect(sound, volume, pitch);
    }

    public SoundEffect withPitch(float pitch) {
        return new SoundEffect(sound, volume, pitch);
    }

    public void playSound(@NonNull Player player) {
        if (sound == null) {
            return;
        }
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public void playSound(@NonNull  SimpleLocation simpleLocation) {
        this.playSound(simpleLocation.asLocation());
    }

    public void playSound(@NonNull Location location) {
        if (sound == null) {
            return;
        }
        final World world = location.getWorld();
        if (world != null) {
            world.playSound(location, sound, volume, pitch);
        }
    }

    protected static Sound getSound(String sound) {
        final String upperCase = sound.toUpperCase();
        return Arrays.stream(Sound.values()).filter(s -> s.name().equals(upperCase)).findFirst().orElse(null);
    }

}
