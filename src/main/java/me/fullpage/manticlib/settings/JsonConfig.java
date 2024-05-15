package me.fullpage.manticlib.settings;

import com.google.gson.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public final class JsonConfig {

    public static Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public String lines;
    private String name;
    private JsonObject jsonObject;
    private Plugin plugin;

    private JsonConfig() {

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

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        if (name == null || name.isEmpty()) return null;
        return new File(plugin.getDataFolder(), this.name);
    }

    public void save() {
        if (name == null || name.isEmpty()) return;
        File file = new File(plugin.getDataFolder(), this.name);

        if (!file.exists() || this.jsonObject == null) return;



        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(GSON.toJson(this.jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
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
