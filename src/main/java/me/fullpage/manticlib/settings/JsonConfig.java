package me.fullpage.manticlib.settings;

import com.google.gson.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.charset.CharacterCodingException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;

public final class JsonConfig {

    public static Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public String lines;
    private String name;
    private JsonObject jsonObject;
    private Plugin plugin;

    private JsonConfig() {

    }

    static JsonConfig forSaving(String name, Plugin plugin) {
        JsonConfig config = new JsonConfig();
        config.name = name;
        config.plugin = plugin;
        config.jsonObject = new JsonObject();
        return config;
    }

    public JsonConfig(String name, Plugin plugin) {
        this(name, plugin, false);
    }

    public JsonConfig(String name, Plugin plugin, boolean readLinesOnly) {
        this.name = name;
        this.plugin = plugin;

        File file = new File(plugin.getDataFolder(), this.name);

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (BufferedReader reader = openReader(file, plugin)) {
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            if (readLinesOnly) {
                this.jsonObject = new JsonObject();
                this.lines = builder.toString();
                return;
            }

            if (builder.length() != 0) {
                this.jsonObject = new JsonParser().parse(builder.toString()).getAsJsonObject();
            } else {
                this.jsonObject = new JsonObject();
            }
        } catch (IOException | RuntimeException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load settings from " + file, e);
            // Create backup of old file
            try {
                Path backup = Files.createTempFile(file.toPath().toAbsolutePath().getParent(), file.getName() + ".backup-", ".json");
                Files.copy(file.toPath(), backup, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException backupError) {
                e.addSuppressed(backupError);
            }
            throw new IllegalStateException("Could not load settings from " + file, e);
        }
    }

    public File getFile() {
        if (name == null || name.isEmpty()) return null;
        return new File(plugin.getDataFolder(), this.name);
    }

    private static BufferedReader openReader(File file, Plugin plugin) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            Charset legacy = Charset.defaultCharset();
            if (legacy.equals(StandardCharsets.UTF_8)) throw e;
            text = legacy.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
            plugin.getLogger().warning("Reading legacy " + legacy + " settings from " + file + "; next save uses UTF-8");
        }
        return new BufferedReader(new StringReader(text));
    }

    public void save() {
        if (name == null || name.isEmpty()) return;
        File file = new File(plugin.getDataFolder(), this.name);

        if (!file.exists() || this.jsonObject == null) return;



        try {
            SettingsPersistence.forPlugin(plugin).submit(file.toPath(), () -> GSON.toJson(this.jsonObject)).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException) throw (RuntimeException) e.getCause();
        }
    }

    public boolean has(String key) {
        return this.jsonObject.has(key);
    }

    public void setString(String index, String value) {
        if (value == null) return;

        this.jsonObject.addProperty(index, value);
    }

    public void set(String index, JsonElement value) {
        if (value == null) return;

        this.jsonObject.add(index, value);
    }

    public void setInteger(String index, int value) {
        this.jsonObject.addProperty(index, value);
    }

    public void setBoolean(String index, boolean value) {
        this.jsonObject.addProperty(index, value);
    }

    public void setLong(String index, long value) {
        this.jsonObject.addProperty(index, value);
    }

    public String getString(String index) {
        return this.jsonObject.has(index) ? this.jsonObject.get(index).getAsString() : "String not found - index: " + index;
    }

    public String getString(String index, String def) {
        return this.jsonObject.has(index) ? this.jsonObject.get(index).getAsString() : def;
    }

    public JsonArray getJsonArray(String index) {
        return  this.jsonObject.has(index) ? this.jsonObject.get(index).getAsJsonArray() : new JsonArray();
    }

    public void setJsonArray(String index, JsonArray jsonArray) {
        this.jsonObject.add(index, jsonArray);
    }

    public int getInt(String index) {
        return this.jsonObject.has(index) ? this.jsonObject.get(index).getAsInt() : 0;
    }

    public int getInt(String index, int def) {
        return this.jsonObject.has(index) ? this.jsonObject.get(index).getAsInt() : def;
    }

    public long getLong(String index) {
        return this.jsonObject.has(index) ? this.jsonObject.get(index).getAsLong() : 0L;
    }

    public long getLong(String index, long def) {
        return this.jsonObject.has(index) ? this.jsonObject.get(index).getAsLong() : def;
    }

    public boolean getBoolean(String index) {
        return this.jsonObject.has(index) && this.jsonObject.get(index).getAsBoolean();
    }

    public boolean getBoolean(String index, boolean def) {
        return this.jsonObject.has(index) ? this.jsonObject.get(index).getAsBoolean() : def;
    }

    public void setJsonObject(JsonObject object) {
        this.jsonObject = object;
    }

    public JsonObject getJsonObject() {
        return this.jsonObject;
    }

    public void delete() {
        if (name == null || name.isEmpty()) return;
        File file = new File(plugin.getDataFolder(), this.name);

        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public String toString() {
        return this.jsonObject.toString();
    }

    public static JsonConfig from(JsonObject jsonObject) {
        final JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.jsonObject = jsonObject;
        jsonConfig.plugin = JavaPlugin.getProvidingPlugin(jsonConfig.getClass());
        jsonConfig.name = null;
        return jsonConfig;
    }
}
