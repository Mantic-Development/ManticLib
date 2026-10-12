package me.fullpage.manticlib.particle;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Cached, cross-version single-particle emitters. Unsupported names return null.
 */
public final class ParticleEmitter {
    private static final Map<String, ParticleEmitter> CACHE = new HashMap<>();
    private static java.util.List<String> names;
    private final Object particle;
    private final Method spawn;
    private final boolean legacy;
    private java.util.function.Function<Location, Object> data = location -> null;

    private ParticleEmitter(Object particle, Method spawn, boolean legacy) {
        this.particle = particle; this.spawn = spawn; this.legacy = legacy;
    }

    /**
     * Resolves a server particle once. Both old and current names are accepted.
     */
    public static synchronized ParticleEmitter of(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        String key = name.trim().toUpperCase(Locale.ROOT);
        if (CACHE.containsKey(key)) return CACHE.get(key);
        ParticleEmitter found = null;
        String[] alternatives = aliases(key);
        String[] aliases = new String[alternatives.length + 1];
        aliases[0] = key; System.arraycopy(alternatives, 0, aliases, 1, alternatives.length);
        try {
            Class<?> type = Class.forName("org.bukkit.Particle");
            for (String alias : aliases) {
                try {
                    Object particle = Enum.valueOf((Class) type, alias);
                    found = new ParticleEmitter(particle, World.class.getMethod("spawnParticle", type, Location.class,
                            int.class, double.class, double.class, double.class, double.class, Object.class), false);
                    found.data = defaultData((Class<?>) type.getMethod("getDataType").invoke(particle));
                    break;
                } catch (IllegalArgumentException ignored) { }
            }
        } catch (ClassNotFoundException missing) {
            for (String alias : aliases) {
                try {
                    Effect effect = Effect.valueOf(alias);
                    if (!effect.getType().name().equals("PARTICLE")) continue;
                    Method method = World.Spigot.class.getMethod("playEffect", Location.class, Effect.class, int.class, int.class,
                            float.class, float.class, float.class, float.class, int.class, int.class);
                    found = new ParticleEmitter(effect, method, true); break;
                } catch (IllegalArgumentException | NoSuchMethodException ignored) { }
            }
        } catch (ReflectiveOperationException | LinkageError ignored) { }
        CACHE.put(key, found);
        return found;
    }

    /**
     * Available server particle names for configuration editors.
     */
    public static synchronized java.util.List<String> names() {
        if (names != null) return names;
        java.util.List<String> available = new java.util.ArrayList<>();
        try {
            for (Object particle : Class.forName("org.bukkit.Particle").getEnumConstants()) {
                String name = ((Enum<?>) particle).name(); if (of(name) != null) available.add(name);
            }
        } catch (ClassNotFoundException ignored) {
            for (Effect effect : Effect.values()) if (effect.getType().name().equals("PARTICLE") && of(effect.name()) != null) available.add(effect.name());
        }
        java.util.Collections.sort(available);
        names = java.util.Collections.unmodifiableList(available); return names;
    }

    private static java.util.function.Function<Location, Object> defaultData(Class<?> type) throws ReflectiveOperationException {
        Object fixed = null;
        if (type == Float.class) fixed = 1f;
        else if (type == Double.class) fixed = 1d;
        else if (type == Integer.class) fixed = 1;
        else if (type == Long.class) fixed = 1L;
        else if (type == Short.class) fixed = (short) 1;
        else if (type == Byte.class) fixed = (byte) 1;
        else if (type == Boolean.class) fixed = true;
        else if (type == org.bukkit.Color.class) fixed = org.bukkit.Color.WHITE;
        else if (type == org.bukkit.inventory.ItemStack.class) fixed = new org.bukkit.inventory.ItemStack(org.bukkit.Material.STONE);
        else if (type.getName().equals("org.bukkit.material.MaterialData")) fixed = type.getConstructor(org.bukkit.Material.class).newInstance(org.bukkit.Material.STONE);
        else if (type.getName().equals("org.bukkit.block.data.BlockData")) fixed = org.bukkit.Material.class.getMethod("createBlockData").invoke(org.bukkit.Material.STONE);
        else if (type.getName().endsWith("$DustOptions") || type.getName().endsWith("$Spell")) fixed = type.getConstructor(org.bukkit.Color.class, float.class).newInstance(org.bukkit.Color.WHITE, 1f);
        else if (type.getName().endsWith("$Geyser")) fixed = type.getConstructor(int.class).newInstance(1);
        else if (type.getName().endsWith("$GeyserBase")) fixed = type.getConstructor(int.class, float.class).newInstance(1, 1f);
        else if (type.getName().endsWith("$DustTransition")) fixed = type.getConstructor(org.bukkit.Color.class, org.bukkit.Color.class, float.class).newInstance(org.bukkit.Color.WHITE, org.bukkit.Color.BLACK, 1f);
        else if (type.getName().endsWith("$Trail")) {
            java.lang.reflect.Constructor<?> constructor = type.getConstructor(Location.class, org.bukkit.Color.class, int.class);
            return location -> construct(constructor, location.clone().add(0, 2, 0), org.bukkit.Color.WHITE, 60);
        } else if (type.getName().equals("org.bukkit.Vibration")) {
            Class<?> destination = Class.forName("org.bukkit.Vibration$Destination");
            java.lang.reflect.Constructor<?> block = Class.forName("org.bukkit.Vibration$Destination$BlockDestination").getConstructor(Location.class);
            java.lang.reflect.Constructor<?> vibration = type.getConstructor(Location.class, destination, int.class);
            return location -> construct(vibration, location, construct(block, location.clone().add(0, 2, 0)), 60);
        }
        final Object value = fixed;
        return location -> value;
    }

    private static Object construct(java.lang.reflect.Constructor<?> constructor, Object... values) {
        try { return constructor.newInstance(values); }
        catch (ReflectiveOperationException error) { throw new IllegalStateException("Could not prepare particle data", error); }
    }

    private static String[] aliases(String name) {
        String[][] names = {{"FIREWORK", "FIREWORKS_SPARK"}, {"ENCHANT", "ENCHANTMENT_TABLE", "ENCHANTMENT_TABLE_PARTICLE"},
                {"WITCH", "SPELL_WITCH", "WITCH_MAGIC"}, {"TOTEM_OF_UNDYING", "TOTEM"}, {"DUST", "REDSTONE", "COLOURED_DUST"},
                {"POOF", "EXPLOSION_NORMAL", "EXPLOSION"}, {"SMOKE", "SMOKE_NORMAL"}, {"LARGE_SMOKE", "SMOKE_LARGE"},
                {"HAPPY_VILLAGER", "VILLAGER_HAPPY"}, {"CRIT", "CRITICAL_HIT"}, {"EFFECT", "SPELL", "SPELL_MOB", "POTION_SWIRL"},
                {"INSTANT_EFFECT", "SPELL_INSTANT", "INSTANT_SPELL"}, {"END_ROD"}, {"FLAME"}, {"HEART"}, {"EXPLOSION_EMITTER", "EXPLOSION_HUGE", "HUGE_EXPLOSION"},
                {"EXPLOSION", "EXPLOSION_LARGE", "LARGE_EXPLODE"}, {"ENTITY_EFFECT", "SPELL_MOB_AMBIENT", "POTION_SWIRL_TRANSPARENT"},
                {"ENCHANTED_HIT", "CRIT_MAGIC", "MAGIC_CRIT"}, {"DRIPPING_WATER", "DRIP_WATER", "WATERDRIP"},
                {"DRIPPING_LAVA", "DRIP_LAVA", "LAVADRIP"}, {"SPLASH", "WATER_SPLASH"}, {"BUBBLE", "WATER_BUBBLE"},
                {"FISHING", "WATER_WAKE"}, {"UNDERWATER", "SUSPENDED"}, {"ASH", "SUSPENDED_DEPTH"},
                {"ANGRY_VILLAGER", "VILLAGER_ANGRY"}, {"MYCELIUM", "TOWN_AURA"}, {"SNOWBALL", "SNOWBALL_POOF"},
                {"ITEM_SLIME", "SLIME"}, {"ITEM", "ITEM_CRACK"}, {"BLOCK", "BLOCK_CRACK", "TILE_BREAK"},
                {"FALLING_DUST", "BLOCK_DUST", "TILE_DUST"}};
        for (String[] group : names) for (String alias : group) if (alias.equals(name)) return group;
        return new String[]{name};
    }

    /**
     * Spawns one particle with no random offset. Diagnostics are formatted only on failure.
     */
    public void spawn(Location location) {
        if (location == null) throw new IllegalArgumentException("Particle location is required");
        World world = location.getWorld();
        if (world == null) throw new IllegalArgumentException("Particle world is missing at " + location);
        if (!Double.isFinite(location.getX()) || !Double.isFinite(location.getY()) || !Double.isFinite(location.getZ()))
            throw new IllegalArgumentException("Particle coordinates must be finite");
        try {
            if (legacy) spawn.invoke(world.spigot(), location, particle, 0, 0, 0f, 0f, 0f, 0f, 1, 32);
            else spawn.invoke(world, particle, location, 1, 0d, 0d, 0d, 0d, data.apply(location));
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Could not spawn particle " + particle, error);
        }
    }
}
