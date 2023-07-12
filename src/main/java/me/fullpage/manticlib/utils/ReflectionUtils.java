package me.fullpage.manticlib.utils;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * @author <a href="https://github.com/Despical/Commons">...</a> and myself
 */
public final class ReflectionUtils {

    public static final String VERSION = parseVersion();

    public static final int VER = Integer.parseInt(VERSION.substring(1).split("_")[1]);

    public static final String
            CRAFTBUKKIT = "org.bukkit.craftbukkit." + VERSION + '.',
            NMS = v(17, "net.minecraft.").orElse("net.minecraft.server." + VERSION + '.');

    private static final MethodHandle PLAYER_CONNECTION, GET_HANDLE, SEND_PACKET;
    public static final Class<?> entityPlayer;
    @Deprecated
    public static final Class<?> CRAFT_PLAYER;

    static {
        entityPlayer = getNMSClass("server.level", "EntityPlayer");
        CRAFT_PLAYER = entityPlayer;
        Class<?> craftPlayer = getCraftClass("entity.CraftPlayer"), playerConnection = getNMSClass("server.network", "PlayerConnection");

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle sendPacket = null, getHandle = null, connection = null;

        try {
            connection = lookup.findGetter(entityPlayer, v(20, "c").orElse(v(17, "b").orElse("playerConnection")), playerConnection);
            getHandle = lookup.findVirtual(craftPlayer, "getHandle", MethodType.methodType(entityPlayer));
            sendPacket = lookup.findVirtual(playerConnection, v(18, "a").orElse("sendPacket"), MethodType.methodType(void.class, getNMSClass("network.protocol", "Packet")));
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        PLAYER_CONNECTION = connection;
        SEND_PACKET = sendPacket;
        GET_HANDLE = getHandle;
    }

    private ReflectionUtils() {
    }

    private static String parseVersion() {
        String found = null;

        for (Package pack : Package.getPackages()) {
            if (pack.getName().startsWith("org.bukkit.craftbukkit.v")) {
                found = pack.getName().split("\\.")[3];
                break;
            }
        }

        if (found == null)
            throw new IllegalArgumentException("Failed to parse server version. Could not find any package starting with name: 'org.bukkit.craftbukkit.v'");
        return found;
    }

    public static <T> VersionHandler<T> v(int version, T handle) {
        return new VersionHandler<>(version, handle);
    }

    public static <T> CallableVersionHandler<T> v(int version, Callable<T> handle) {
        return new CallableVersionHandler<>(version, handle);
    }

    /**
     * Checks whether the server version is equal or greater than the given version.
     *
     * @param version the version to compare the server version with.
     * @return true if the version is equal or newer, otherwise false.
     */
    public static boolean supports(int version) {
        return VER >= version;
    }

    /**
     * Get a NMS (net.minecraft.server) class.
     *
     * @param name the name of the class.
     * @return the NMS class or null if not found.
     */
    @Nullable
    public static Class<?> getNMSClass(@Nonnull String name) {
        try {
            return Class.forName(NMS + name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Sends a packet to the player synchronously if they're online.
     *
     * @param player  the player to send the packet to.
     * @param packets the packets to send.
     * @see #sendPacket(Player, Object...)
     */
    public static void sendPacketSync(@Nonnull Player player, @Nonnull Object... packets) {
        try {
            Object handle = GET_HANDLE.invoke(player);
            Object connection = PLAYER_CONNECTION.invoke(handle);

            if (connection != null) {
                for (Object packet : packets) SEND_PACKET.invoke(connection, packet);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Nullable
    public static Object getHandle(@Nonnull Player player) {
        Objects.requireNonNull(player, "Cannot get handle of null player");
        try {
            return GET_HANDLE.invoke(player);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object getConnection(@Nonnull Player player) {
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
     */
    @Nullable
    public static Class<?> getCraftClass(@Nonnull String name) {
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

    public static final class VersionHandler<T> {
        private int version;
        private T handle;

        private VersionHandler(int version, T handle) {
            if (supports(version)) {
                this.version = version;
                this.handle = handle;
            }
        }

        public VersionHandler<T> v(int version, T handle) {
            if (version == this.version)
                throw new IllegalArgumentException("Cannot have duplicate version handles for version: " + version);
            if (version > this.version && supports(version)) {
                this.version = version;
                this.handle = handle;
            }
            return this;
        }

        public T orElse(T handle) {
            return this.version == 0 ? handle : this.handle;
        }
    }

    public static final class CallableVersionHandler<T> {
        private int version;
        private Callable<T> handle;

        private CallableVersionHandler(int version, Callable<T> handle) {
            if (supports(version)) {
                this.version = version;
                this.handle = handle;
            }
        }

        public CallableVersionHandler<T> v(int version, Callable<T> handle) {
            if (version == this.version)
                throw new IllegalArgumentException("Cannot have duplicate version handles for version: " + version);
            if (version > this.version && supports(version)) {
                this.version = version;
                this.handle = handle;
            }
            return this;
        }

        public T orElse(Callable<T> handle) {
            try {
                return (this.version == 0 ? handle : this.handle).call();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
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

    // find method with parameter types
    public static Optional<Method> findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName) && Arrays.equals(method.getParameterTypes(), parameterTypes)) {
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

    public static boolean hasNoArgConstructor(Class<?> cls) {
        return Arrays.stream(cls.getDeclaredConstructors())
                .anyMatch(c -> c.getParameterCount() == 0);
    }

    public static <T> T newInstance(Class<T> cls) {
        try {
            Constructor<T> constructor = cls.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            String msg = "Class " + cls.getSimpleName() + " doesn't have a " +
                    "no-args constructor.";
            throw new RuntimeException(msg, e);
        } catch (IllegalAccessException e) {
            /* This exception should not be thrown because
             * we set the field to be accessible. */
            String msg = "No-args constructor of class " + cls.getSimpleName() +
                    " not accessible.";
            throw new RuntimeException(msg, e);
        } catch (InstantiationException e) {
            String msg = "Class " + cls.getSimpleName() + " not instantiable.";
            throw new RuntimeException(msg, e);
        } catch (InvocationTargetException e) {
            String msg = "Constructor of class " + cls.getSimpleName() +
                    " has thrown an exception.";
            throw new RuntimeException(msg, e);
        }
    }

    public static <T> T newInstance(Class<T> cls, Object... instances) {
        try {
            Class<?>[] classes = Arrays.stream(instances)
                    .map(Object::getClass)
                    .toArray(Class<?>[]::new);
            Constructor<T> constructor = cls.getDeclaredConstructor(classes);
            constructor.setAccessible(true);
            return constructor.newInstance(instances);
        } catch (NoSuchMethodException e) {
            String msg = "Class " + cls.getSimpleName() + " doesn't have a " +
                    "constructor with the specified parameter types.";
            throw new RuntimeException(msg, e);
        } catch (IllegalAccessException e) {
            /* This exception should not be thrown because
             * we set the field to be accessible. */
            String msg = "Constructor of class " + cls.getSimpleName() +
                    " not accessible.";
            throw new RuntimeException(msg, e);
        } catch (InstantiationException e) {
            String msg = "Class " + cls.getSimpleName() + " not instantiable.";
            throw new RuntimeException(msg, e);
        } catch (InvocationTargetException e) {
            String msg = "Constructor of class " + cls.getSimpleName() +
                    " has thrown an exception.";
            throw new RuntimeException(msg, e);
        }


    }

}