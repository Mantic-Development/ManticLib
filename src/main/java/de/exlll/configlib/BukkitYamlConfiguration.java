package de.exlll.configlib;

import de.exlll.configlib.format.FieldNameFormatters;
import de.exlll.configlib.yaml.YamlConfiguration;
import me.fullpage.manticlib.utils.ReflectionUtils;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.nio.file.Path;

/**
 * A {@code BukkitYamlConfiguration} is a specialized form of a
 * {@code YamlConfiguration} that uses better default values.
 */
public abstract class BukkitYamlConfiguration extends YamlConfiguration {
    protected BukkitYamlConfiguration(@NotNull Path path, @NotNull BukkitYamlProperties properties) {
        super(path, properties);
    }
    protected BukkitYamlConfiguration(@NotNull File file, @NotNull BukkitYamlProperties properties) {
        super(file.toPath(), properties);
    }

    protected BukkitYamlConfiguration(@NotNull Path path) {
        this(path, BukkitYamlProperties.DEFAULT);
    }
    protected BukkitYamlConfiguration(@NotNull File file) {
        this(file.toPath(), BukkitYamlProperties.DEFAULT);
    }

    protected BukkitYamlConfiguration(@NotNull Plugin plugin, @NotNull String name) {
        this(new File(plugin.getDataFolder(), name.replace(".yml", "").replace(".json", "").replaceAll("[^a-zA-Z\\d]", "_") + ".yml"), BukkitYamlProperties.DEFAULT);
    }

    public static class BukkitYamlProperties extends YamlProperties {
        public static final BukkitYamlProperties DEFAULT = builder().setFormatter(FieldNameFormatters.LOWER_HYPHON).build();

        private BukkitYamlProperties(Builder<?> builder) {
            super(builder);
        }

        public static Builder<?> builder() {
            return new Builder() {
                @Override
                protected Builder<?> getThis() {
                    return this;
                }
            };
        }

        public static abstract class
        Builder<B extends Builder<B>>
                extends YamlProperties.Builder<B> {

            protected Builder() {
                setConstructor(this.newConstructor());
                setRepresenter(this.newRepresenter());
            }
            private Constructor newConstructor() {
                if (ReflectionUtils.hasNoArgConstructor(Constructor.class)) {
                    return ReflectionUtils.newInstance(Constructor.class);
                }
                return ReflectionUtils.newInstance(Constructor.class, new LoaderOptions());
            }
            private Representer newRepresenter() {
                if (ReflectionUtils.hasNoArgConstructor(Representer.class)) {
                    return ReflectionUtils.newInstance(Representer.class);
                }
                return ReflectionUtils.newInstance(Representer.class, new DumperOptions());
            }

            /**
             * Builds a new {@code BukkitYamlProperties} instance using the values set.
             *
             * @return new {@code BukkitYamlProperties} instance
             */
            public BukkitYamlProperties build() {
                return new BukkitYamlProperties(this);
            }
        }
    }
}
