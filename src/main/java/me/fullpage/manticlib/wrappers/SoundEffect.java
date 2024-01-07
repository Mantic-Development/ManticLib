package me.fullpage.manticlib.wrappers;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.fullpage.manticlib.collections.WeightedList;
import me.fullpage.manticlib.utils.ReflectionUtils;
import me.fullpage.manticlib.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
public class SoundEffect {

    private Sound sound;
    private String customSound;
    private final float volume, pitch;

    public SoundEffect(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public SoundEffect(String sound, float volume, float pitch) {
        this(sound != null && sound.startsWith("CUSTOM:") ? null : getSound(sound), volume, pitch);
        if (sound != null && this.sound == null) { 
            customSound = sound.substring(7);
        }
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
        if (customSound != null) {
            player.playSound(player.getLocation(), customSound, volume, pitch);
            return;

        }
        if (sound == null) {
            return;
        }
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public void playSound(@NonNull SimpleLocation simpleLocation) {
        this.playSound(simpleLocation.asLocation());
    }

    public void playSound(@NonNull Location location) {
        if (customSound != null && ReflectionUtils.supports(12)) {
            World world = location.getWorld();
            if (world != null) {
                world.playSound(location, customSound, volume, pitch);
            }
            return;

        }
        if (sound == null) {
            return;
        }

        World world = location.getWorld();
        if (world != null) {
            world.playSound(location, sound, volume, pitch);
        }
    }

    protected static Sound getSound(String sound) {
        if (Utils.isNullOrEmpty(sound)) {
            return null;
        }
        String upperCase = sound.toUpperCase();

        Set<Sound> possibilities = new HashSet<>();
        Sound parsed;
        try {
            parsed = Sound.valueOf(upperCase);
        } catch (IllegalArgumentException e) {
            Sound found = null;
            for (Sound s : Sound.values()) {
                if (s.name().equals(upperCase)) {
                    found = s;
                    break;
                } else if (s.name().endsWith(upperCase)) {
                    possibilities.add(s);
                }
            }
            parsed = found;
        }

        if (parsed == null && !possibilities.isEmpty()) {
            if (possibilities.size() == 1) {
                parsed = possibilities.iterator().next();
            } else { // attempt to find the best match
                String[] parts = upperCase.split("_");
                WeightedList<Sound> weighted = new WeightedList<>();
                for (Sound s : possibilities) {
                    String[] sParts = s.name().split("_");
                    int matches = 0;
                    for (String part : parts) {
                        for (String sPart : sParts) {
                            if (part.equals(sPart)) {
                                matches++;
                            }
                        }
                    }
                    weighted.add(s, matches);
                }
                parsed = weighted.getHighest();
            }
        }


        return parsed;
    }

    public SoundEffect orElse(@NonNull String sound) {
        if (this.sound != null) {
            return this;
        }
        this.sound = getSound(sound);
        return this;
    }

}
