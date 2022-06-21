package me.fullpage.manticlib.settings;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import me.fullpage.manticlib.interfaces.Registrable;
import me.fullpage.manticlib.interfaces.Reloadable;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;

public class Settings<S extends Settings<S>> implements Registrable, Reloadable {

    private transient Settings<S> instance = null;
    private transient JsonConfig config = null;
    private transient String prePath = null;

    @Override
    public void register() {
        this.reload();
    }

    @Override
    public void reload() {
        config = new JsonConfig(getFileString(), JavaPlugin.getProvidingPlugin(getClass()));
        applyFields();
    }

    public void save() {
        final File file = config.getFile();
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        instance = this;
        apply((S)instance);

        try (FileWriter writer = new FileWriter(file)) {
            JsonConfig.GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            this.instance = JsonConfig.GSON.fromJson(reader, this.getClass());
        }
    }

    @SuppressWarnings("unchecked")
    private void applyFields() {
        if (config.getFile() == null) {
            throw new IllegalStateException("config should not be null");
        }
        final JsonObject jsonObject = config.getJsonObject();
        try (FileReader reader = new FileReader(config.getFile())) {
            this.instance = JsonConfig.GSON.fromJson(reader, this.getClass());
            this.checkOrAdd(this.getClass().getDeclaredFields(), jsonObject, reader);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                Class<?>[] prams = this.getClass().getDeclaredConstructors()[0].getParameterTypes();
                Constructor<Settings<S>> constructor = (Constructor<Settings<S>>) this.getClass().getDeclaredConstructor(prams);
                constructor.setAccessible(true);
                Object[] inputPrams = new Object[prams.length];
                for (int i = 0; i < prams.length; i++) {
                    Class<?> clazz = prams[i];
                    if (clazz.isPrimitive()) {
                        if (clazz.equals(int.class)) {
                            inputPrams[i] = 0;
                        } else if (clazz.equals(byte.class)) {
                            inputPrams[i] = (byte) 0;
                        } else if (clazz.equals(short.class)) {
                            inputPrams[i] = (short) 0;
                        } else if (clazz.equals(long.class)) {
                            inputPrams[i] =  0L;
                        } else if (clazz.equals(float.class)) {
                            inputPrams[i] =  0.0f;
                        } else if (clazz.equals(double.class)) {
                            inputPrams[i] = 0.0;
                        } else if (clazz.equals(boolean.class)) {
                            inputPrams[i] = false;
                        } else if (clazz.equals(char.class)) {
                            inputPrams[i] = ' ';
                        }
                    }
                }
                instance = constructor.newInstance(inputPrams);
                final Field[] declaredFields = getClass().getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    if (declaredField == null || Modifier.isTransient(declaredField.getModifiers()) || Modifier.isStatic(declaredField.getModifiers()) || Modifier.isFinal(declaredField.getModifiers())) {
                        continue;
                    }
                    declaredField.setAccessible(true);
                    declaredField.set(this, declaredField.get(instance));
                }
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        } catch (JsonSyntaxException | NullPointerException e) {
            createBackupFile();
            e.printStackTrace();
        }
        config.save();
        apply((S)instance);
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
                    Files.move(file.toPath(), new File(file + ".backup-" + i).toPath());
                } catch (IOException e) {
                    JavaPlugin.getProvidingPlugin(getClass()).getLogger().severe("Unable to create backup file for " + file.getName());
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
        Field[] declaredFields = instance.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField == null || Modifier.isTransient(declaredField.getModifiers()) || Modifier.isStatic(declaredField.getModifiers())) {
                continue;
            }
            try {
                declaredField.setAccessible(true);
                declaredField.set(instance, declaredField.get(that));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}
