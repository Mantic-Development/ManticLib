package me.fullpage.manticlib.utils;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="https://github.com/Despical/Commons">...</a> and myself
 */
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
     * <p>
     * <a href="https://www.spigotmc.org/wiki/spigot-nms-and-minecraft-versions-legacy/">Versions Legacy</a>
     */
    public static final String NMS_VERSION;

    static { // This needs to be right below VERSION because of initialization order.
        // This package loop is used to avoid implementation-dependant strings like Bukkit.getVersion() or Bukkit.getBukkitVersion()
        // which allows easier testing as well.
        String found = null;
        for (Package pack : Package.getPackages()) {
            String name = pack.getName();

            // .v because there are other packages.
            if (name.startsWith("org.bukkit.craftbukkit.v")) {
                found = pack.getName().split("\\.")[3];

                // Just a final guard to make sure it finds this important class.
                // As a protection for forge+bukkit implementation that tend to mix versions.
                // The real CraftPlayer should exist in the package.
                // Note: Doesn't seem to function properly. Will need to separate the version
                // handler for NMS and CraftBukkit for softwares like catmc.
                try {
                    Class.forName("org.bukkit.craftbukkit." + found + ".entity.CraftPlayer");
                    break;
                } catch (ClassNotFoundException e) {
                    found = null;
                }
            }
        }
        if (found == null)
            throw new IllegalArgumentException("Failed to parse server version. Could not find any package starting with name: 'org.bukkit.craftbukkit.v'");
        NMS_VERSION = found;
        VERSION = NMS_VERSION;
    }

    /**
     * The raw minor version number.
     * E.g. {@code v1_17_R1} to {@code 17}
     *
     * @see #supports(int)
     * @since 4.0.0
     */
    public static final int MINOR_NUMBER;
    public static final int VER;
    public static final String VERSION;
    /**
     * The raw patch version number.
     * E.g. {@code v1_17_R1} to {@code 1}
     * <p>
     * I'd not recommend developers to support individual patches at all. You should always support the latest patch.
     * For example, between v1.14.0, v1.14.1, v1.14.2, v1.14.3 and v1.14.4 you should only support v1.14.4
     * <p>
     * This can be used to warn server owners when your plugin will break on older patches.
     *
     * @see #supportsPatch(int)
     * @since 7.0.0
     */
    public static final int PATCH_NUMBER;

    static {
        String[] split = NMS_VERSION.substring(1).split("_");
        if (split.length < 1) {
            throw new IllegalStateException("Version number division error: " + Arrays.toString(split) + ' ' + getVersionInformation());
        }

        String minorVer = split[1];
        try {
            MINOR_NUMBER = Integer.parseInt(minorVer);
            VER = MINOR_NUMBER;
            if (MINOR_NUMBER < 0)
                throw new IllegalStateException("Negative minor number? " + minorVer + ' ' + getVersionInformation());
        } catch (Throwable ex) {
            throw new RuntimeException("Failed to parse minor number: " + minorVer + ' ' + getVersionInformation(), ex);
        }

        // Bukkit.getBukkitVersion() = "1.12.2-R0.1-SNAPSHOT"
        Matcher bukkitVer = Pattern.compile("^\\d+\\.\\d+\\.(\\d+)").matcher(Bukkit.getBukkitVersion());
        if (bukkitVer.find()) { // matches() won't work, we just want to match the start using "^"
            try {
                // group(0) gives the whole matched string, we just want the captured group.
                PATCH_NUMBER = Integer.parseInt(bukkitVer.group(1));
            } catch (Throwable ex) {
                throw new RuntimeException("Failed to parse minor number: " + bukkitVer + ' ' + getVersionInformation(), ex);
            }
        } else {
            // 1.8-R0.1-SNAPSHOT
            PATCH_NUMBER = 0;
        }
    }

    /**
     * Gets the full version information of the server. Useful for including in errors.
     *
     * @since 7.0.0
     */
    public static String getVersionInformation() {
        return "(NMS: " + NMS_VERSION + " | " +
                "Minecraft: " + Bukkit.getVersion() + " | " +
                "Bukkit: " + Bukkit.getBukkitVersion() + ')';
    }

    /**
     * Gets the latest known patch number of the given minor version.
     * For example: 1.14 -> 4, 1.17 -> 10
     * The latest version is expected to get newer patches, so make sure to account for unexpected results.
     *
     * @param minorVersion the minor version to get the patch number of.
     * @return the patch number of the given minor version if recognized, otherwise null.
     * @since 7.0.0
     */
    public static Integer getLatestPatchNumberOf(int minorVersion) {
        if (minorVersion <= 0) throw new IllegalArgumentException("Minor version must be positive: " + minorVersion);

        // https://minecraft.wiki/w/Java_Edition_version_history
        // There are many ways to do this, but this is more visually appealing.
        int[] patches = {
                /* 1 */ 1,
                /* 2 */ 5,
                /* 3 */ 2,
                /* 4 */ 7,
                /* 5 */ 2,
                /* 6 */ 4,
                /* 7 */ 10,
                /* 8 */ 8, // I don't think they released a server version for 1.8.9
                /* 9 */ 4,

                /* 10 */ 2,//          ,_  _  _,
                /* 11 */ 2,//            \o-o/
                /* 12 */ 2,//           ,(.-.),
                /* 13 */ 2,//         _/ |) (| \_
                /* 14 */ 4,//           /\=-=/\
                /* 15 */ 2,//          ,| \=/ |,
                /* 16 */ 5,//        _/ \  |  / \_
                /* 17 */ 1,//            \_!_/
                /* 18 */ 2,
                /* 19 */ 4,
                /* 20 */ 2,
        };

        if (minorVersion > patches.length) return null;
        return patches[minorVersion - 1];
    }

    /**
     * Mojang remapped their NMS in 1.17: <a href="https://www.spigotmc.org/threads/spigot-bungeecord-1-17.510208/#post-4184317">Spigot Thread</a>
     */
    public static final String
            CRAFTBUKKIT_PACKAGE = "org.bukkit.craftbukkit." + NMS_VERSION + '.',
            NMS_PACKAGE = v(17, "net.minecraft.").orElse("net.minecraft.server." + NMS_VERSION + '.');
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
    private static final Class<?> entityPlayer;

    static {
        entityPlayer = getNMSClass("server.level", "EntityPlayer");
        Class<?> craftPlayer = getCraftClass("entity.CraftPlayer");
        Class<?> playerConnection = getNMSClass("server.network", "PlayerConnection");
        Class<?> playerCommonConnection;
        if (supports(20) && supportsPatch(2)) {
            // The packet send method has been abstracted from ServerGamePacketListenerImpl to ServerCommonPacketListenerImpl in 1.20.2
            playerCommonConnection = getNMSClass("server.network", "ServerCommonPacketListenerImpl");
        } else {
            playerCommonConnection = playerConnection;
        }

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle sendPacket = null, getHandle = null, connection = null;

        try {
            connection = lookup.findGetter(entityPlayer,
                    v(20, "c").v(17, "b").orElse("playerConnection"), playerConnection);
            getHandle = lookup.findVirtual(craftPlayer, "getHandle", MethodType.methodType(entityPlayer));
            sendPacket = lookup.findVirtual(playerCommonConnection,
                    v(20, 2, "b").v(18, "a").orElse("sendPacket"),
                    MethodType.methodType(void.class, getNMSClass("network.protocol", "Packet")));
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
     * This method is purely for readability.
     * No performance is gained.
     *
     * @since 5.0.0
     */
    public static <T> VersionHandler<T> v(int version, T handle) {
        return new VersionHandler<>(version, handle);
    }

    /**
     * Overload for {@link #v(int, T)} that supports patch versions
     *
     * @since 9.5.0
     */
    public static <T> VersionHandler<T> v(int version, int patch, T handle) {
        return new VersionHandler<>(version, patch, handle);
    }

    public static <T> CallableVersionHandler<T> v(int version, Callable<T> handle) {
        return new CallableVersionHandler<>(version, handle);
    }

    /**
     * Checks whether the server version is equal or greater than the given version.
     *
     * @param minorNumber the version to compare the server version with.
     * @return true if the version is equal or newer, otherwise false.
     * @see #MINOR_NUMBER
     * @since 4.0.0
     */
    public static boolean supports(int minorNumber) {
        return MINOR_NUMBER >= minorNumber;
    }

    /**
     * Checks whether the server version is equal or greater than the given version.
     *
     * @param minorNumber the minor version to compare the server version with.
     * @param patchNumber the patch number to compare the server version with.
     * @return true if the version is equal or newer, otherwise false.
     * @see #MINOR_NUMBER
     * @see #PATCH_NUMBER
     * @since 7.1.0
     */
    public static boolean supports(int minorNumber, int patchNumber) {
        return MINOR_NUMBER == minorNumber ? supportsPatch(patchNumber) : supports(minorNumber);
    }

    /**
     * Checks whether the server version is equal or greater than the given version.
     *
     * @param patchNumber the version to compare the server version with.
     * @return true if the version is equal or newer, otherwise false.
     * @see #PATCH_NUMBER
     * @since 7.0.0
     */
    public static boolean supportsPatch(int patchNumber) {
        return PATCH_NUMBER >= patchNumber;
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
    public static Class<?> getNMSClass(@Nonnull String newPackage, @Nonnull String name) {
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
    public static Class<?> getNMSClass(@Nonnull String name) {
        try {
            return Class.forName(NMS_PACKAGE + name);
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
    @Nonnull
    public static CompletableFuture<Void> sendPacket(@Nonnull Player player, @Nonnull Object... packets) {
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
    public static void sendPacketSync(@Nonnull Player player, @Nonnull Object... packets) {
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
     * @since 1.0.0
     */
    @Nullable
    public static Class<?> getCraftClass(@Nonnull String name) {
        try {
            return Class.forName(CRAFTBUKKIT_PACKAGE + name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Class<?> getArrayClass(String clazz, boolean nms) {
        clazz = "[L" + (nms ? NMS_PACKAGE : CRAFTBUKKIT_PACKAGE) + clazz + ';';
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
        private int patch;
        private T handle;

        private VersionHandler(int version, T handle) {
            this(version, 0, handle);
        }

        private VersionHandler(int version, int patch, T handle) {
            if (supports(version) && supportsPatch(patch)) {
                this.version = version;
                this.patch = patch;
                this.handle = handle;
            }
        }

        public VersionHandler<T> v(int version, T handle) {
            return v(version, 0, handle);
        }

        public VersionHandler<T> v(int version, int patch, T handle) {
            if (version == this.version && patch == this.patch)
                throw new IllegalArgumentException("Cannot have duplicate version handles for version: " + version + '.' + patch);
            if (version > this.version && supports(version) && patch >= this.patch && supportsPatch(patch)) {
                this.version = version;
                this.patch = patch;
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

    @Deprecated
    public static boolean isOrAbove(int version) {
        return version >= MINOR_NUMBER;
    }

    @Deprecated
    public static boolean isAbove(int version) {
        return version > MINOR_NUMBER;
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

    public static boolean hasArgConstructor(Class<?> cls, Class<?>... parameterTypes) {
        return Arrays.stream(cls.getDeclaredConstructors())
                .anyMatch(c -> Arrays.equals(c.getParameterTypes(), parameterTypes));
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

    @SneakyThrows
    public static int getPing(Player p) {
        Class<? extends Player> playerClass = p.getClass();
        if (!playerClass.getName().equals("org.bukkit.craftbukkit." + VERSION + ".entity.CraftPlayer")) { //compatibility with some plugins
            p = Bukkit.getPlayer(p.getUniqueId()); //cast to org.bukkit.entity.Player
        }

        if (p == null) {
            return -1;
        }

        Object handle = ReflectionUtils.getHandle(p);
        if (handle != null) {
             Optional<Field> ping = ReflectionUtils.findField(handle.getClass(), "ping");
            if (ping.isPresent()) {
                return ping.get().getInt(handle);
            }
        }

        Optional<Method> getPing = ReflectionUtils.findMethod(ReflectionUtils.entityPlayer, "getPing");
        if (getPing.isPresent()) {
            return (int) getPing.get().invoke(p);
        }

        return -1;
    }

}