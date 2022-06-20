package me.fullpage.manticlib.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class ReflectionUtils {
    /**
     * We use reflection mainly to avoid writing a new class for version barrier.
     * The version barrier is for NMS that uses the Minecraft version as the main package name.
     * <p>
     * E.g. EntityPlayer in 1.15 is in the class {@code net.minecraft.server.v1_15_R1}
     * but in 1.14 it's in {@code net.minecraft.server.v1_14_R1}
     * In order to maintain cross-version compatibility we cannot import these classes.
     * <p>
     * Performance is not a concern for these specific statically initialized values.
     */
    public static final String
            VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    /**
     * The raw minor version number.
     * E.g. {@code v1_17_R1} to {@code 17}
     *
     * @since 4.0.0
     */
    public static final int VER = Integer.parseInt(VERSION.substring(1).split("_")[1]);
    /**
     * Mojang remapped their NMS in 1.17 https://www.spigotmc.org/threads/spigot-bungeecord-1-17.510208/#post-4184317
     */
    public static final String
            CRAFTBUKKIT = "org.bukkit.craftbukkit." + VERSION + '.',
            NMS = supports(17) ? "net.minecraft." : "net.minecraft.server." + VERSION + '.';
    /**
     * A nullable public accessible field only available in {@code EntityPlayer}.
     * This can be null if the player is offline.
     */
    private static final MethodHandle PLAYER_CONNECTION;
    /**
     * Responsible for getting the NMS handler {@code EntityPlayer} object for the player.
     * {@code CraftPlayer} is simply a wrapper for {@code EntityPlayer}.
     * Used mainly for handling packet related operations.
     * <p>
     * This is also where the famous player {@code ping} field comes from!
     */
    private static final MethodHandle GET_HANDLE;
    /**
     * Sends a packet to the player's client through a {@code NetworkManager} which
     * is where {@code ProtocolLib} controls packets by injecting channels!
     */
    private static final MethodHandle SEND_PACKET;

    public static final Class<?> CRAFT_PLAYER;

    static {
        Class<?> entityPlayer = getNMSClass("server.level", "EntityPlayer");
        CRAFT_PLAYER = getCraftClass("entity.CraftPlayer");
        Class<?> playerConnection = getNMSClass("server.network", "PlayerConnection");

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle sendPacket = null;
        MethodHandle getHandle = null;
        MethodHandle connection = null;
        try {
            connection = lookup.findGetter(entityPlayer, supports(17) ? "b" : "playerConnection", playerConnection);
            getHandle = lookup.findVirtual(CRAFT_PLAYER, "getHandle", MethodType.methodType(entityPlayer));
            sendPacket = lookup.findVirtual(playerConnection, "sendPacket", MethodType.methodType(void.class, getNMSClass("network.protocol", "Packet")));
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        PLAYER_CONNECTION = connection;
        SEND_PACKET = sendPacket;
        GET_HANDLE = getHandle;
    }

    private ReflectionUtils() {
    }

    /**
     * Checks whether the server version is equal or greater than the given version.
     *
     * @param version the version to compare the server version with.
     * @return true if the version is equal or newer, otherwise false.
     * @since 4.0.0
     */
    public static boolean supports(int version) {
        return VER >= version;
    }

    /**
     * Get a NMS (net.minecraft.server) class which accepts a package for 1.17 compatibility.
     *
     * @param newPackage the 1.17 package name.
     * @param name       the name of the class.
     * @return the NMS class or null if not found.
     * @since 4.0.0
     */
    @Nullable
    public static Class<?> getNMSClass(@NotNull String newPackage, @NotNull String name) {
        if (supports(17)) name = newPackage + '.' + name;
        return getNMSClass(name);
    }

    /**
     * Get a NMS (net.minecraft.server) class.
     *
     * @param name the name of the class.
     * @return the NMS class or null if not found.
     * @since 1.0.0
     */
    @Nullable
    public static Class<?> getNMSClass(@NotNull String name) {
        try {
            return Class.forName(NMS + name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Sends a packet to the player asynchronously if they're online.
     * Packets are thread-safe.
     *
     * @param player  the player to send the packet to.
     * @param packets the packets to send.
     * @return the async thread handling the packet.
     * @see #sendPacketSync(Player, Object...)
     * @since 1.0.0
     */
    @NotNull
    public static CompletableFuture<Void> sendPacket(@NotNull Player player, @NotNull Object... packets) {
        return CompletableFuture.runAsync(() -> sendPacketSync(player, packets))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    /**
     * Sends a packet to the player synchronously if they're online.
     *
     * @param player  the player to send the packet to.
     * @param packets the packets to send.
     * @see #sendPacket(Player, Object...)
     * @since 2.0.0
     */
    public static void sendPacketSync(@NotNull Player player, @NotNull Object... packets) {
        try {
            Object handle = GET_HANDLE.invoke(player);
            Object connection = PLAYER_CONNECTION.invoke(handle);

            // Checking if the connection is not null is enough. There is no need to check if the player is online.
            if (connection != null) {
                for (Object packet : packets) SEND_PACKET.invoke(connection, packet);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Nullable
    public static Object getHandle(@NotNull Player player) {
        Objects.requireNonNull(player, "Cannot get handle of null player");
        try {
            return GET_HANDLE.invoke(player);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object getConnection(@NotNull Player player) {
        Objects.requireNonNull(player, "Cannot get connection of null player");
        try {
            Object handle = GET_HANDLE.invoke(player);
            return PLAYER_CONNECTION.invoke(handle);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    /**
     * Get a CraftBukkit (org.bukkit.craftbukkit) class.
     *
     * @param name the name of the class to load.
     * @return the CraftBukkit class or null if not found.
     * @since 1.0.0
     */
    @Nullable
    public static Class<?> getCraftClass(@NotNull String name) {
        try {
            return Class.forName(CRAFTBUKKIT + name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Class<?> getArrayClass(String clazz, boolean nms) {
        clazz = "[L" + (nms ? NMS : CRAFTBUKKIT) + clazz + ';';
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Class<?> toArrayClass(Class<?> clazz) {
        try {
            return Class.forName("[L" + clazz.getName() + ';');
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static boolean isOrAbove(int version) {
        return version >= VER;
    }

    public static boolean isAbove(int version) {
        return version > VER;
    }

    public static Field getField(Class<?> clazz, String name) {
        if (clazz == null) throw new NullPointerException("clazz");
        if (name == null) throw new NullPointerException("name");
        try {
            Field ret = clazz.getDeclaredField(name);
            makeAccessible(ret);
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void makeAccessible(Field field) {
        Objects.requireNonNull(field, "No field provided");
        if ((!isAccessible(field) || Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    public static <T extends AccessibleObject & Member> boolean isAccessible(T member) {
        Objects.requireNonNull(member, "No member provided");
        return Modifier.isPublic((member).getModifiers()) && Modifier.isPublic((member).getDeclaringClass().getModifiers());
    }


    @SuppressWarnings("unchecked")
    public static <T> T getField(Field field, Object object) {
        try {
            return (T) field.get(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------------------------------- //
    // FIELD > SET
    // -------------------------------------------- //

    public static void setField(Field field, Object object, Object value) {
        try {
            field.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------------------------------- //
    // FIELD > SIMPLE
    // -------------------------------------------- //

    public static <T> T getField(Class<?> clazz, String name, Object object) {
        Field field = getField(clazz, name);
        return getField(field, object);
    }

    // Other:

    public static Optional<Method> findMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }

    public static Optional<Field> findField(Class<?> clazz, String fieldName) {
        for (Field field : clazz.getFields()) {
            if (field.getName().equals(fieldName)) {
                return Optional.of(field);
            }
        }
        return Optional.empty();
    }

}