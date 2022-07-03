package me.fullpage.manticlib.settings.yml;

import com.google.common.base.CaseFormat;
import lombok.SneakyThrows;
import me.fullpage.manticlib.interfaces.Registrable;
import me.fullpage.manticlib.interfaces.Reloadable;
import me.fullpage.manticlib.settings.yml.annotations.Comment;
import me.fullpage.manticlib.settings.yml.annotations.ConfigComponent;
import me.fullpage.manticlib.settings.yml.annotations.Name;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import sun.reflect.ReflectionFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

public class Settings<S extends Settings<S>> implements Registrable, Reloadable {

    private transient File file = null;
    private FileConfiguration fileConfiguration = null;
    private transient String prePath = null;
    private transient Mode mode = Mode.FIELD_NAME;

    public Mode getMode() {
        return mode == null ? Mode.FIELD_NAME : mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public void register() {
        this.reload();
    }

    @Override
    public void reload() {
        this.checkFile();
        this.applyFields();
    }

    private void checkFile() {
        if (file == null) {
            file = new File(JavaPlugin.getProvidingPlugin(getClass()).getDataFolder(), getFileString());
        }
    }

    @SneakyThrows
    public void save() {
        this.checkFile();
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        fileConfiguration.save(file);
    }

    private boolean isInvalidField(Field field) {
        if (field == null) {
            return true;
        }

        Class<?> fieldClass = field.getType();
        if (!this.getClass().isAssignableFrom(fieldClass) && fieldClass.isAnnotationPresent(ConfigComponent.class) || this.isStringOrPrimitive(field)) {
            return false;
        }

        return Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers());
    }

    private boolean isStringOrPrimitive(Field field) {
        return field.getType() == String.class || field.getType().isPrimitive();
    }

    // TODO: 03/07/2022 add list support

    private void checkOrAdd(final Field[] declaredFields) {
        boolean changed = false;
        for (Field declaredField : declaredFields) {
            if (isInvalidField(declaredField)) {
                continue;
            }

            try {
                declaredField.setAccessible(true);
                final String name = this.getFieldSerialisedName(declaredField);
                boolean isSet = fileConfiguration.isSet(name);
                if (!isSet) {
                    if (declaredField.isAnnotationPresent(Comment.class)) {
                        String[] value = declaredField.getAnnotation(Comment.class).value();
                        if (value.length > 0) {
                            fileConfiguration.set(name + "_COMMENT", String.join("\n", value).replaceAll(":", "{{{REPLACE_ME_WITH_DOUBLE_DOTS}}}"));
                            changed = true;
                        }
                    }

                }
                if (this.isStringOrPrimitive(declaredField)) {
                    if (!isSet) {
                        fileConfiguration.set(name, declaredField.get(this));
                        changed = true;
                    }
                    declaredField.set(this, fileConfiguration.get(name));
                } else if (declaredField.getType().isAnnotationPresent(ConfigComponent.class)) {
                    final ReflectionFactory reflection = ReflectionFactory.getReflectionFactory();
                    final Constructor<Object> constructor = (Constructor<Object>) reflection.newConstructorForSerialization(declaredField.getType(), Object.class.getDeclaredConstructor());
                    final Object o = constructor.newInstance();

                    for (Field field : o.getClass().getDeclaredFields()) {
                        if (isInvalidField(field)) {
                            continue;
                        }

                        if (isStringOrPrimitive(field)) {
                            field.setAccessible(true);

                            if (!isSet) {
                                fileConfiguration.set(name + "." + getFieldSerialisedName(field), field.get(o));
                                changed = true;
                            }

                            field.set(o, fileConfiguration.get(name + "." + getFieldSerialisedName(field)));
                        } else if (Collection.class.isAssignableFrom(field.getType())) {
                            if (!isSet) {
                                fileConfiguration.set(name + "." + getFieldSerialisedName(field), field.get(o));
                                changed = true;
                            }
                            field.set(this, fileConfiguration.getList(name));
                        }
                    }

                    declaredField.set(this, o);

                } else if (Collection.class.isAssignableFrom(declaredField.getType())) {
                    //  ParameterizedType stringListType = (ParameterizedType) declaredField.getGenericType();
                    //   Class<?> listClass = (Class<?>) stringListType.getActualTypeArguments()[0];

                    if (!isSet) {
                        fileConfiguration.set(name, declaredField.get(this));
                        changed = true;
                    }

                    declaredField.set(this, fileConfiguration.getList(name));


                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (changed) {
            this.save();
        }

    }

    private void setupYaml() {
        try {
            fileConfiguration = YamlConfiguration.loadConfiguration(file);
            InputStream defConfigStream = JavaPlugin.getProvidingPlugin(getClass()).getResource(this.getFileString());
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
                fileConfiguration.setDefaults(defConfig);
            }
        } catch (Exception e) {
            Logger.getLogger("Minecraft").warning("An error occurred whilst trying to reload " + file.toPath());
        }
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private void applyFields() {
        this.checkFile();
        this.setupYaml();
        this.checkOrAdd(this.getClass().getDeclaredFields());
        fileConfiguration.save(file);
        apply((S) this);
    }


    private String getFieldSerialisedName(Field field) {
        if (field.isAnnotationPresent(Name.class)) {
            return field.getAnnotation(Name.class).value();
        } else {
            switch (this.getMode()) {
                case UPPER_UNDERSCORE:
                    return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, field.getName());
                case LOWER_UNDERSCORE:
                    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
                case LOWER_HYPHON:
                    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, field.getName());
                case UPPER_HYPHON:
                    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, field.getName()).toUpperCase();
                default:
                    return field.getName();
            }
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
        return (getPrePath() == null ? "" : getPrePath()) + name.toLowerCase().trim().replace('.', '_') + ".yml";
    }

    private void createBackupFile() {
        HashSet<String> files = new HashSet<>();

        this.checkFile();
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
        // copy(this, that);
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

    public enum Mode {
        UPPER_UNDERSCORE,
        LOWER_UNDERSCORE,
        UPPER_HYPHON,
        LOWER_HYPHON,
        FIELD_NAME
    }

}
