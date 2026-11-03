package me.fullpage.manticlib.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.fullpage.manticlib.ManticLib;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public final class SkullUtils {

    @FunctionalInterface
    public interface SkinListener {
        void onSkinUpdate(UUID id, ItemStack head);
    }

    private static final long REFRESH_AFTER = 6L * 60L * 60L * 1000L;

    private static final Material SKULL;
    private static final boolean LEGACY;
    private static final Class<?> PROFILE_API;
    private static final Method CREATE_PROFILE;
    private static final Method GET_TEXTURES;
    private static final Method SET_TEXTURES;
    private static final Method SET_SKIN;
    private static final Method GET_SKIN;
    private static final Method SET_OWNER;
    private static final Method PLAYER_PROFILE;

    private static final Map<String, ItemStack> CUSTOM = new ConcurrentHashMap<>();
    private static final Map<UUID, HeadEntry> HEADS = new ConcurrentHashMap<>();
    private static final Map<UUID, Skin> SKINS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> RETRY = new ConcurrentHashMap<>();
    private static final Map<UUID, String> LOCAL = new ConcurrentHashMap<>();

    private static final Queue<Lookup> QUEUE = new ConcurrentLinkedQueue<>();
    private static final Set<UUID> PENDING =
            Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
    private static final List<SkinListener> LISTENERS = new CopyOnWriteArrayList<>();

    private static final AtomicInteger ACTIVE = new AtomicInteger();
    private static final AtomicBoolean SAVE_QUEUED = new AtomicBoolean();
    private static final AtomicLong REVISION = new AtomicLong();
    private static final AtomicLong GENERATION = new AtomicLong();

    private static final Object FILE_LOCK = new Object();
    private static final Object CACHE_LOCK = new Object();

    private static volatile long pauseUntil;
    private static volatile boolean started;
    private static volatile boolean loaded;
    private static volatile boolean stopping;

    static {
        Material material = Material.matchMaterial("PLAYER_HEAD");
        LEGACY = material == null;
        SKULL = LEGACY ? Material.matchMaterial("SKULL_ITEM") : material;

        if (SKULL == null) {
            throw new IllegalStateException("No player skull material available");
        }

        Class<?> profile = null;
        Method[] api = null;

        try {
            Class<?> p = Class.forName("org.bukkit.profile.PlayerProfile");
            Class<?> t = Class.forName("org.bukkit.profile.PlayerTextures");

            api = new Method[]{
                    Bukkit.class.getMethod("createPlayerProfile", UUID.class, String.class),
                    p.getMethod("getTextures"),
                    p.getMethod("setTextures", t),
                    t.getMethod("setSkin", URL.class),
                    t.getMethod("getSkin"),
                    SkullMeta.class.getMethod("setOwnerProfile", p)
            };

            profile = p;

        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Older versions use legacy GameProfile.
            api = null;
        }

        Method playerProfile = null;

        try {
            playerProfile = Player.class.getMethod("getPlayerProfile");
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Not available on this version.
        }

        PROFILE_API = profile;
        CREATE_PROFILE = api == null ? null : api[0];
        GET_TEXTURES = api == null ? null : api[1];
        SET_TEXTURES = api == null ? null : api[2];
        SET_SKIN = api == null ? null : api[3];
        GET_SKIN = api == null ? null : api[4];
        SET_OWNER = api == null ? null : api[5];
        PLAYER_PROFILE = playerProfile;
    }

    private SkullUtils() {
    }

    /**
     * Call from ManticLib.onEnable() on the main server thread.
     */
    public static void init() {
        loadCache();
        startWorker();
    }

    /**
     * Call from ManticLib.onDisable() to flush any pending skin-cache changes.
     */
    public static void shutdown() {
        stopping = true;
        GENERATION.incrementAndGet();
        QUEUE.clear();
        synchronized (CACHE_LOCK) {
            if (loaded) {
                saveCache();
            }
        }
    }

    public static void addListener(SkinListener listener) {
        LISTENERS.add(Objects.requireNonNull(listener, "listener"));
    }

    public static void removeListener(SkinListener listener) {
        LISTENERS.remove(listener);
    }

    private static ItemStack newHead() {
        return LEGACY ? new ItemStack(SKULL, 1, (short) 3) : new ItemStack(SKULL);
    }

    public static ItemStack getSkull(String base64) {
        if (base64 == null || base64.trim().isEmpty()) {
            return newHead();
        }

        String key = base64.trim();
        ItemStack cached = CUSTOM.get(key);

        if (cached != null) {
            return cached.clone();
        }

        Skin skin = skinOf(key);
        ItemStack head = skin == null
                ? null
                : texturedHead(UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)), skin);

        if (head == null) {
            return newHead();
        }

        CUSTOM.putIfAbsent(key, head.clone());
        return head;
    }

    public static ItemStack getHead(Player player) {
        Objects.requireNonNull(player, "player");
        return head(player.getUniqueId(), player.getName(), player);
    }

    public static ItemStack getHead(UUID id, String name) {
        Objects.requireNonNull(id, "id");

        Player online = Bukkit.getPlayer(id);
        if (name == null && online != null) {
            name = online.getName();
        }

        return head(id, name, online);
    }

    private static ItemStack head(UUID id, String name, Player online) {
        loadCache();

        if (online != null) {
            syncLocal(online);
        }

        ItemStack head = headFor(id);

        if (head == null) {
            queueLookup(id, name, false);
            return newHead();
        }

        // Refresh expired skins even if the player is online.
        Skin skin = SKINS.get(id);
        if (skin != null
                && System.currentTimeMillis() - skin.checked > REFRESH_AFTER) {
            queueLookup(id, name, true);
        }

        return head;
    }

    /**
     * Entries are version-tagged with their source Skin object. A concurrent
     * update cannot leave an older head permanently cached under a new skin.
     */
    private static ItemStack headFor(UUID id) {
        for (int attempt = 0; attempt < 3; attempt++) {
            Skin skin = SKINS.get(id);
            if (skin == null) {
                return null;
            }

            HeadEntry cached = HEADS.get(id);
            if (cached != null && cached.skin == skin) {
                return cached.head.clone();
            }

            ItemStack item = texturedHead(id, skin);
            if (item == null) {
                return null;
            }

            synchronized (CACHE_LOCK) {
                if (SKINS.get(id) != skin) {
                    continue;
                }
                HEADS.put(id, new HeadEntry(skin, item.clone()));
                return item;
            }
        }
        return null;
    }

    /**
     * Optional. getHead() automatically queues missing skins.
     */
    public static void preloadHeads(Map<UUID, String> players) {
        if (players == null || players.isEmpty()) {
            return;
        }

        loadCache();

        for (Map.Entry<UUID, String> entry : players.entrySet()) {
            UUID id = entry.getKey();
            if (id == null || SKINS.containsKey(id)) {
                continue;
            }

            // Only query known names or genuine UUID candidates. Never
            // trigger Paper's own profile resolver to recover a name.
            queueLookup(id, entry.getValue(), false);
        }
    }

    public static void clearCache() {
        GENERATION.incrementAndGet();

        synchronized (CACHE_LOCK) {
            CUSTOM.clear();
            HEADS.clear();
            SKINS.clear();
            RETRY.clear();
            LOCAL.clear();
            QUEUE.clear();
            PENDING.clear();
            REVISION.incrementAndGet();
        }

        synchronized (FILE_LOCK) {
            File file = cacheFile();
            if (file.exists() && !file.delete()) {
                ManticLib.get().getLogger().warning("Could not delete skull cache");
            }
        }
    }

    private static void syncLocal(Player player) {
        Skin observed = localSkin(player);
        if (observed == null) {
            return;
        }

        UUID id = player.getUniqueId();
        String lastSeen = LOCAL.put(id, observed.url);

        // A locally observed texture is used when first cached or when the
        // online player's actual texture changes. An unchanged local profile
        // must NOT replace a more recent response from Mojang.
        if (!SKINS.containsKey(id)
                || (lastSeen != null && !lastSeen.equals(observed.url))) {
            remember(id, observed);
        }
    }

    private static Skin localSkin(Player player) {
        Skin local = legacyTexture(player);
        if (local != null) {
            return local; // Includes Mojang signature when available.
        }

        if (PLAYER_PROFILE != null && PROFILE_API != null) {
            try {
                Object textures = GET_TEXTURES.invoke(PLAYER_PROFILE.invoke(player));
                URL url = (URL) GET_SKIN.invoke(textures);
                return url == null ? null : skinOf(encodeSkin(url.toExternalForm()));
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                // No locally available skin.
            }
        }
        return null;
    }

    private static Skin legacyTexture(Player player) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object value = handle.getClass().getMethod("getProfile").invoke(handle);

            if (!(value instanceof GameProfile)) {
                return null;
            }

            for (Property property : ((GameProfile) value).getProperties().get("textures")) {
                Method method;
                try {
                    method = property.getClass().getMethod("value");
                } catch (NoSuchMethodException e) {
                    method = property.getClass().getMethod("getValue");
                }

                Object texture = method.invoke(property);
                if (texture instanceof String) {
                    String signature = null;
                    try {
                        signature = String.valueOf(property.getClass()
                                .getMethod("signature").invoke(property));
                    } catch (NoSuchMethodException e) {
                        try {
                            signature = String.valueOf(property.getClass()
                                    .getMethod("getSignature").invoke(property));
                        } catch (ReflectiveOperationException ignored) {
                            // Unsigned legacy texture.
                        }
                    }
                    if ("null".equals(signature)) signature = null;
                    Skin result = skinOf((String) texture, System.currentTimeMillis(), signature);
                    if (result != null) return result;
                }
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            // No locally loaded texture.
        }
        return null;
    }

    private static ItemStack texturedHead(UUID id, Skin skin) {
        ItemStack head = newHead();
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta == null) {
            return null;
        }

        try {
            if (PROFILE_API != null) {
                Object profile = CREATE_PROFILE.invoke(null, id, null);
                Object textures = GET_TEXTURES.invoke(profile);

                SET_SKIN.invoke(textures, new URL(skin.url));
                SET_TEXTURES.invoke(profile, textures);
                SET_OWNER.invoke(meta, profile);

            } else {
                GameProfile profile = new GameProfile(id, "");
                profile.getProperties().put("textures", skin.signature == null
                        ? new Property("textures", skin.value)
                        : new Property("textures", skin.value, skin.signature));

                Class<?> type = meta.getClass();
                Field field = null;

                while (type != null) {
                    try {
                        field = type.getDeclaredField("profile");
                        break;
                    } catch (NoSuchFieldException ignored) {
                        type = type.getSuperclass();
                    }
                }

                if (field == null) {
                    return null;
                }

                field.setAccessible(true);
                field.set(meta, profile);
            }

            head.setItemMeta(meta);
            return head;

        } catch (ReflectiveOperationException | IllegalArgumentException
                 | IOException | LinkageError ignored) {
            return null;
        }
    }

    private static String encodeSkin(String url) {
        JsonObject skin = new JsonObject();
        skin.addProperty("url", url);

        JsonObject textures = new JsonObject();
        textures.add("SKIN", skin);

        JsonObject root = new JsonObject();
        root.add("textures", textures);

        return Base64.getEncoder().encodeToString(root.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeUrl(String base64) {
        try {
            String json = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);

            String url = new JsonParser()
                    .parse(json)
                    .getAsJsonObject()
                    .getAsJsonObject("textures")
                    .getAsJsonObject("SKIN")
                    .get("url")
                    .getAsString();

            new URL(url);
            return url;

        } catch (Exception ignored) {
            return null;
        }
    }

    private static Skin skinOf(String base64) {
        return skinOf(base64, System.currentTimeMillis(), null);
    }

    private static Skin skinOf(String base64, long checked, String signature) {
        String url = base64 == null ? null : decodeUrl(base64);
        return url == null ? null : new Skin(base64, url, checked, signature);
    }

    /**
     * Used for local player profile changes, not remote freshness checks.
     */
    private static void remember(UUID id, Skin skin) {
        if (skin == null || stopping) return;
        boolean changed;
        synchronized (CACHE_LOCK) {
            if (stopping) return;
            Skin previous = SKINS.get(id);
            changed = previous == null || !previous.url.equals(skin.url);
            if (!changed && previous != null && previous.signature != null
                    && skin.signature == null) {
                return; // Keep existing signed data.
            }
            if (!changed && previous != null && previous.value.equals(skin.value)
                    && Objects.equals(previous.signature, skin.signature)) {
                return; // Don't mark the Mojang check as fresh.
            }
            SKINS.put(id, skin);
            HEADS.remove(id);
            REVISION.incrementAndGet();
        }
        scheduleSave();
        if (changed) notifyListeners(id);
    }

    /**
     * A successful Mojang fetch refreshes the verified time even unchanged.
     */
    private static void confirm(UUID id, Skin skin) {
        if (skin == null || stopping) return;
        boolean changed;
        synchronized (CACHE_LOCK) {
            if (stopping) return;
            Skin previous = SKINS.get(id);
            changed = previous == null || !previous.url.equals(skin.url);
            if (previous != null && !changed
                    && previous.value.equals(skin.value)
                    && Objects.equals(previous.signature, skin.signature)) {
                previous.checked = skin.checked;
            } else {
                SKINS.put(id, skin);
                HEADS.remove(id);
            }
            REVISION.incrementAndGet();
        }
        scheduleSave();
        if (changed) notifyListeners(id);
    }

    private static void notifyListeners(UUID id) {
        if (LISTENERS.isEmpty()) {
            return;
        }

        try {
            Bukkit.getScheduler().runTask(ManticLib.get(), () -> {
                ItemStack head = headFor(id);

                if (head == null) {
                    return;
                }

                for (SkinListener listener : LISTENERS) {
                    try {
                        listener.onSkinUpdate(id, head.clone());
                    } catch (Exception e) {
                        ManticLib.get().getLogger().log(Level.WARNING, "SkullUtils listener failed", e);
                    }
                }
            });

        } catch (RuntimeException ignored) {
            // Plugin disabled.
        }
    }

    private static String validName(String name) {
        return name != null && name.matches("[A-Za-z0-9_]{3,16}") ? name : null;
    }

    private static void queueLookup(UUID id, String name, boolean refresh) {
        if (stopping || (!refresh && SKINS.containsKey(id))
                || RETRY.getOrDefault(id, 0L) > System.currentTimeMillis()
                || (id.version() != 4 && validName(name) == null)
                || !PENDING.add(id)) {
            return;
        }

        QUEUE.offer(new Lookup(id, validName(name), GENERATION.get(), refresh));
        startWorker();
    }

    private static synchronized void startWorker() {
        if (started || stopping) {
            return;
        }

        try {
            Bukkit.getScheduler().runTaskTimer(ManticLib.get(), SkullUtils::processQueue, 20L, 20L);
            Bukkit.getPluginManager().registerEvents(new JoinListener(), ManticLib.get());
            started = true;

        } catch (RuntimeException ignored) {
            // Retry after plugin enable.
        }
    }

    private static void processQueue() {
        if (stopping || ACTIVE.get() >= 2
                || System.currentTimeMillis() < pauseUntil) {
            return;
        }

        // At most one new request per second, maximum two in flight.
        Lookup lookup = QUEUE.poll();
        if (lookup == null) return;

        if (lookup.generation != GENERATION.get()
                || (!lookup.refresh && SKINS.containsKey(lookup.id))) {
            PENDING.remove(lookup.id);
            return;
        }

        ACTIVE.incrementAndGet();
        try {
            Bukkit.getScheduler().runTaskAsynchronously(ManticLib.get(), () -> {
                try {
                    Skin skin = fetchSkin(lookup);
                    if (!stopping && lookup.generation == GENERATION.get()) {
                        if (skin != null) {
                            confirm(lookup.id, skin);
                            RETRY.remove(lookup.id);
                        } else {
                            long now = System.currentTimeMillis();
                            RETRY.put(lookup.id, pauseUntil > now
                                    ? pauseUntil + 1000L : now + 600000L);
                        }
                    }
                } catch (RuntimeException ignored) {
                    // Network and parsing errors are handled quietly.
                } finally {
                    PENDING.remove(lookup.id);
                    ACTIVE.decrementAndGet();
                }
            });
        } catch (RuntimeException ignored) {
            PENDING.remove(lookup.id);
            ACTIVE.decrementAndGet();
        }
    }

    private static Skin fetchSkin(Lookup lookup) {
        // A v4 UUID is only a candidate, not proof of a Mojang identity.
        if (lookup.id.version() == 4) {
            Skin skin = skinByUuid(lookup.id);
            if (skin != null || System.currentTimeMillis() < pauseUntil) {
                return skin;
            }
        }

        if (lookup.name == null) return null;

        JsonObject identity = fetchJson(
                "https://api.minecraftservices.com/minecraft/profile/lookup/name/" + lookup.name);
        if (identity == null || !identity.has("id")) return null;

        try {
            String raw = identity.get("id").getAsString();
            if (!raw.matches("[0-9a-fA-F]{32}")) return null;
            UUID official = UUID.fromString(raw.replaceFirst(
                    "(?i)([0-9a-f]{8})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{12})",
                    "$1-$2-$3-$4-$5"));
            return skinByUuid(official);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static Skin skinByUuid(UUID id) {
        JsonObject result = fetchJson(
                "https://sessionserver.mojang.com/session/minecraft/profile/"
                        + id.toString().replace("-", "") + "?unsigned=false");
        if (result == null || !result.has("properties")) return null;

        try {
            JsonArray properties = result.getAsJsonArray("properties");
            for (JsonElement element : properties) {
                JsonObject property = element.getAsJsonObject();
                if ("textures".equals(property.get("name").getAsString())) {
                    String value = property.get("value").getAsString();
                    String signature = property.has("signature")
                            ? property.get("signature").getAsString() : null;
                    return skinOf(value, System.currentTimeMillis(), signature);
                }
            }
        } catch (RuntimeException ignored) {
            // Invalid response.
        }
        return null;
    }

    private static JsonObject fetchJson(String url) {
        if (System.currentTimeMillis() < pauseUntil) {
            return null;
        }

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();

            connection.setConnectTimeout(4000);
            connection.setReadTimeout(5000);
            connection.setUseCaches(false);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "ManticLib");

            int status = connection.getResponseCode();

            if (status == 429) {
                // Mojang rate limit: pause requests for 15 minutes.
                pauseFor(900000L);
                return null;
            }

            if (status == 403 || status >= 500) {
                pauseFor(60000L);
                return null;
            }

            if (status != 200) {
                return null;
            }

            try (InputStream input = connection.getInputStream();
                 InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                return new JsonParser().parse(reader).getAsJsonObject();
            }

        } catch (IOException | RuntimeException ignored) {
            // Timeout, connection failure or malformed response.
            // No stack trace is printed to the server console.
            pauseFor(60000L);
            return null;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static synchronized void pauseFor(long millis) {
        pauseUntil = Math.max(pauseUntil, System.currentTimeMillis() + millis);
    }

    private static File cacheFile() {
        return new File(new File(ManticLib.get().getDataFolder(), "data"), "skull-cache.properties");
    }

    private static void loadCache() {
        if (loaded) {
            return;
        }

        synchronized (FILE_LOCK) {
            if (loaded) {
                return;
            }

            File file = cacheFile();

            if (file.isFile()) {
                Properties properties = new Properties();

                try (InputStream input = new FileInputStream(file)) {
                    properties.load(input);

                    for (String key : properties.stringPropertyNames()) {
                        try {
                            String value = properties.getProperty(key);
                            long checked = System.currentTimeMillis();
                            int bar = value.indexOf('|');

                            if (bar > 0) {
                                checked = Long.parseLong(value.substring(0, bar));
                                value = value.substring(bar + 1);
                            }

                            String signature = null;
                            int signatureBar = value.indexOf('|');
                            if (signatureBar >= 0) {
                                signature = value.substring(signatureBar + 1);
                                value = value.substring(0, signatureBar);
                                if (signature.isEmpty()) signature = null;
                            }
                            Skin skin = skinOf(value, checked, signature);

                            if (skin != null) {
                                SKINS.putIfAbsent(UUID.fromString(key), skin);
                            }

                        } catch (IllegalArgumentException ignored) {
                            // Skip invalid cache entries.
                        }
                    }

                } catch (IOException ignored) {
                    // Treat unreadable cache as empty.
                }
            }

            loaded = true;
        }

        startWorker();
    }

    private static void scheduleSave() {
        if (!SAVE_QUEUED.compareAndSet(false, true)) {
            return;
        }

        try {
            Bukkit.getScheduler().runTaskLaterAsynchronously(ManticLib.get(), () -> {
                long revision = REVISION.get();

                try {
                    saveCache();
                } finally {
                    SAVE_QUEUED.set(false);

                    if (REVISION.get() != revision) {
                        scheduleSave();
                    }
                }
            }, 100L);

        } catch (RuntimeException ignored) {
            SAVE_QUEUED.set(false);
        }
    }

    private static void saveCache() {
        synchronized (FILE_LOCK) {
            File file = cacheFile();
            File folder = file.getParentFile();

            if (!folder.isDirectory() && !folder.mkdirs()) {
                return;
            }

            Properties properties = new Properties();

            for (Map.Entry<UUID, Skin> entry : SKINS.entrySet()) {
                properties.setProperty(
                        entry.getKey().toString(),
                        entry.getValue().checked + "|" + entry.getValue().value
                                + (entry.getValue().signature == null ? "" : "|" + entry.getValue().signature));
            }

            File temp = new File(folder, "skull-cache.properties.tmp");

            try (OutputStream output = new FileOutputStream(temp)) {
                properties.store(output, "ManticLib cached player skull textures");
            } catch (IOException ignored) {
                return;
            }

            try {
                try {
                    Files.move(temp.toPath(), file.toPath(),
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

            } catch (IOException ignored) {
                // An existing cache remains intact.
            }
        }
    }

    private static final class Skin {
        private final String value;
        private final String url;
        private final String signature;
        private volatile long checked;

        private Skin(String value, String url, long checked, String signature) {
            this.value = value;
            this.url = url;
            this.checked = checked;
            this.signature = signature;
        }
    }

    private static final class HeadEntry {
        private final Skin skin;
        private final ItemStack head;

        private HeadEntry(Skin skin, ItemStack head) {
            this.skin = skin;
            this.head = head;
        }
    }

    private static final class Lookup {

        private final UUID id;
        private final String name;
        private final long generation;
        private final boolean refresh;

        private Lookup(UUID id, String name, long generation, boolean refresh) {
            this.id = id;
            this.name = name;
            this.generation = generation;
            this.refresh = refresh;
        }
    }

    private static final class JoinListener implements Listener {

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            loadCache();
            Player player = event.getPlayer();
            UUID id = player.getUniqueId();
            LOCAL.remove(id);
            Skin joined = localSkin(player);
            if (joined != null) {
                LOCAL.put(id, joined.url);
                // Login profiles are usually current; do not promote this
                // to a Mojang refresh timestamp unless verified remotely.
                remember(id, joined);
            }
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            LOCAL.remove(event.getPlayer().getUniqueId());
        }
    }
}
