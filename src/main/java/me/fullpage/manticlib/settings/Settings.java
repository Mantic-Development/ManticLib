package me.fullpage.manticlib.settings;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import me.fullpage.manticlib.ManticLib;
import me.fullpage.manticlib.interfaces.Registrable;
import me.fullpage.manticlib.interfaces.Reloadable;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;

public class Settings<S extends Settings<S>> implements Registrable, Reloadable {

    private transient Settings<S> instance = null;
    private transient JsonConfig config = null;
    private transient String prePath = null;
    private transient Plugin plugin = null;

    public Plugin getPlugin() {
        if (plugin == null) {
            try {
                plugin = ManticLib.getProvidingPlugin(this.getClass());
            } catch (IllegalStateException e) {
                throw new IllegalStateException("\033[1;31mPlease do not use plugins like \"Plugman\" to load or unload a plugin during runtime. Instead use built-in reload commands in plugins or restart where possible.", e);
            }
        }
        return plugin;
    }

    /**
     * @see #register(Plugin) instead
     */
    @Deprecated
    @Override
    public void register() {
        this.reload();
    }

    public void register(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.reload();
    }

    @Override
    public void reload() {
        Plugin plugin = getPlugin();
        String fileString = getFileString();
        config = new JsonConfig(fileString, plugin);
        applyFields();
    }

    public void save() {
        try {
            saveAsync().join();
        } catch (CompletionException e) {
            // The shared writer already logged the destination and cause.
            if (e.getCause() instanceof RuntimeException) throw (RuntimeException) e.getCause();
        }
    }

    /**
     * Captures the data immediately, then queues it for saving. Errors will cause the save to fail.
     */
    public CompletableFuture<Void> saveAsync() {
        Plugin owner = getPlugin();
        if (config == null) config = JsonConfig.forSaving(getFileString(), owner);
        File file = config.getFile();
        return SettingsPersistence.forPlugin(owner).submit(file.toPath(), () -> {
            instance = this;
            apply((S) instance);
            return JsonConfig.GSON.toJson(this);
        });
    }

    /**
     * Finishes pending saves before shutting down. No new saves can be made after shutdown.
     */
    public static boolean shutdownSaves(Plugin plugin, long timeout, TimeUnit unit) throws InterruptedException {
        return SettingsPersistence.forPlugin(plugin).shutdown(timeout, unit);
    }

    private void checkOrAdd(final Field[] declaredFields, final JsonObject jsonObject, FileReader reader) {
        boolean changed = false;
        for (Field declaredField : declaredFields) {
            if (declaredField == null || Modifier.isTransient(declaredField.getModifiers()) || Modifier.isStatic(declaredField.getModifiers()) || Modifier.isFinal(declaredField.getModifiers())) {
                continue;
            }
            try {
                declaredField.setAccessible(true);
                final String name = getFieldSerialisedName(declaredField);
                if (config.has(name)) {
                    declaredField.set(this, declaredField.get(instance));
                } else {
                    changed = true;
                    jsonObject.add(name, JsonConfig.GSON.toJsonTree(declaredField.get(this), declaredField.getType()));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (changed) {
            this.instance = JsonConfig.GSON.fromJson(jsonObject, this.getClass());
        }
    }

    @SuppressWarnings("unchecked")
    private void applyFields() {
        if (config.getFile() == null) {
            throw new IllegalStateException("config should not be null");
        }
        final JsonObject jsonObject = config.getJsonObject();
        try {
            this.instance = JsonConfig.GSON.fromJson(jsonObject, this.getClass());
            this.checkOrAdd(this.getClass().getDeclaredFields(), jsonObject, null);
        } catch (JsonSyntaxException | NullPointerException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Could not load settings from " + config.getFile(), e);
            createBackupFile();
            throw e;
        }
        config.save();
        apply((S) instance);
    }


    private String getFieldSerialisedName(Field field) {
        if (field.isAnnotationPresent(SerializedName.class)) {
            return field.getAnnotation(SerializedName.class).value();
        } else {
            return field.getName();
        }
    }

    public String getPrePath() {
        return prePath;
    }

    public void setPrePath(String prePath) {
        this.prePath = prePath;
    }

    public String getFileString() {
        final String nameWithPackage;
        final String name;
        final String className = this.getClass().getName();
        if (className.contains("$")) {
            nameWithPackage = className.substring(className.lastIndexOf("$") + 1);
        } else {
            nameWithPackage = className;
        }
        if (nameWithPackage.contains(".")) {
            name = nameWithPackage.substring(className.lastIndexOf(".") + 1);
        } else {
            name = nameWithPackage;
        }
        return (getPrePath() == null ? "" : getPrePath()) + name.toLowerCase().trim().replace('.', '_') + ".json";
    }

    private void createBackupFile() {
        HashSet<String> files = new HashSet<>();

        final File file = config.getFile();
        File[] parentFolderFiles = file.getParentFile().listFiles();

        if (parentFolderFiles == null) {
            return;
        }

        Arrays.stream(parentFolderFiles).forEach(f -> files.add(f.getName()));

        String newFileName = file.getName() + ".backup-";
        for (int i = 1; ; i++) {
            if (!files.contains(newFileName + i)) {
                try {
                    Files.copy(file.toPath(), new File(file + ".backup-" + i).toPath());
                } catch (IOException e) {
                    this.getPlugin().getLogger().severe("Unable to create backup file for " + file.getName());
                }
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected S apply(S that) {
        copy(this, that);
        return (S) this;
    }

    public static <T> void copy(T instance, T that) {
        iterateFields(instance, that);
    }

    private static <T> void iterateFields(T instance, T that) {
        if (instance == null || that == null || instance.equals(that)) {
            return;
        }
        Field[] declaredFields = instance.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField == null || Modifier.isTransient(declaredField.getModifiers()) || Modifier.isStatic(declaredField.getModifiers())) {
                continue;
            }
            try {
                declaredField.setAccessible(true);
                Object value = declaredField.get(that);
                if (value == null) continue;
                declaredField.set(instance, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
