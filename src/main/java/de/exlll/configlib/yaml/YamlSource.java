package de.exlll.configlib.yaml;

import de.exlll.configlib.CommentedMap;
import de.exlll.configlib.Comments;
import de.exlll.configlib.ConfigurationSource;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.*;

final class YamlSource implements ConfigurationSource<YamlConfiguration> {
    @Getter
    private final Path configPath;
    private final YamlConfiguration.YamlProperties props;
    private final Yaml yaml;

    public YamlSource(Path configPath, YamlConfiguration.YamlProperties props) {
        this.configPath = Objects.requireNonNull(configPath);
        this.props = props;
        this.yaml = new Yaml(
                props.getConstructor(), props.getRepresenter(),
                props.getOptions(), props.getResolver()
        );
    }

    @Override
    public void saveConfiguration(YamlConfiguration config, Map<String, Object> map)
            throws IOException {
        createParentDirectories();
        map = unshared(map);

        CommentAdder adder = new CommentAdder(
                YamlFieldComments.add(yaml.dump(map), map, yaml), config.getComments(), props
        );

        List<String> lines = Arrays.asList(adder.getCommentedDump().split("\n"));
        Path target = Files.exists(configPath) ? configPath.toRealPath() : configPath;
        if (!Files.isRegularFile(target) || !Files.isWritable(target) || !replace(target, lines)) {
            Files.write(target, lines, StandardCharsets.UTF_8);
        }
    }

    // A crash mid-write must leave the old file, never a truncated one, so an existing file is swapped for a finished copy. Where the
    // swap would show (hard links, an owner or group it cannot keep, a bind-mounted file) the file is rewritten in place instead.
    private static boolean replace(Path target, List<String> lines) throws IOException {
        Path temporary;
        try {
            temporary = Files.createTempFile(target.getParent(), "." + target.getFileName(), ".tmp");
        } catch (IOException e) {
            return false;
        }
        try {
            Files.write(temporary, lines, StandardCharsets.UTF_8);
            if (!keepAttributes(target, temporary)) return false;
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (FileSystemException e) {
            return false;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static boolean keepAttributes(Path from, Path to) throws IOException {
        PosixFileAttributes attributes;
        try {
            attributes = Files.readAttributes(from, PosixFileAttributes.class);
        } catch (UnsupportedOperationException e) {
            return true;
        }
        try {
            if (((Number) Files.getAttribute(from, "unix:nlink")).intValue() > 1) return false;
        } catch (UnsupportedOperationException | IllegalArgumentException ignored) {
        }
        PosixFileAttributeView view = Files.getFileAttributeView(to, PosixFileAttributeView.class);
        // Owner and group first, while the copy is still private to us, so the file's mode never applies to the wrong group.
        try {
            if (!attributes.group().equals(view.readAttributes().group())) view.setGroup(attributes.group());
            if (!attributes.owner().equals(view.getOwner())) view.setOwner(attributes.owner());
        } catch (IOException e) {
            return false;
        }
        view.setPermissions(attributes.permissions());
        return true;
    }

    // SnakeYAML writes a collection reached twice (one shared default, like an empty list) as an anchor and aliases, so editing
    // one of them in the file would change all of them.
    @SuppressWarnings("unchecked")
    private static <T> T unshared(T value) {
        if (value instanceof Map) {
            Map<Object, Object> copy = new LinkedHashMap<>();
            if (value instanceof CommentedMap) {
                // The field comments ride on the map; a plain copy would drop every comment from the file.
                CommentedMap commented = new CommentedMap();
                commented.getFieldComments().putAll(((CommentedMap) value).getFieldComments());
                copy = (Map<Object, Object>) (Map<?, ?>) commented;
            }
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) copy.put(entry.getKey(), unshared(entry.getValue()));
            return (T) copy;
        }
        if (value instanceof Collection) {
            Collection<Object> copy = value instanceof Set ? new LinkedHashSet<>() : new ArrayList<>();
            for (Object element : (Collection<?>) value) copy.add(unshared(element));
            return (T) copy;
        }
        return value;
    }

    private void createParentDirectories() throws IOException {
        Path parentDir = configPath.getParent();
        if (!Files.isDirectory(parentDir)) {
            Files.createDirectories(parentDir);
        }
    }

    @Override
    public Map<String, Object> loadConfiguration(YamlConfiguration config)
            throws IOException {
        String cfg;
        try {
            cfg = readConfig();
        } catch (NoSuchFileException e) {
            return new HashMap<>();
        }
        return yaml.load(cfg);
    }

    private String readConfig() throws IOException {
        List<String> lines = Files.readAllLines(configPath, StandardCharsets.UTF_8);
        return String.join("\n", lines);
    }

    private static final class CommentAdder {
        private final String dump;
        private final Comments comments;
        private final YamlComments yamlComments;
        private final YamlConfiguration.YamlProperties props;
        private final StringBuilder builder;

        private CommentAdder(String dump, Comments comments,
                             YamlConfiguration.YamlProperties props
        ) {
            this.dump = dump;
            this.props = props;
            this.comments = comments;
            this.yamlComments = new YamlComments(comments);
            this.builder = new StringBuilder(dump.length());
        }

        public String getCommentedDump() {
            addComments(props.getPrependedComments());
            addClassComments();
            builder.append(dump);
            addComments(props.getAppendedComments());
            return builder.toString();
        }

        private void addComments(List<String> comments) {
            for (String comment : comments) {
                if (!comment.isEmpty()) {
                    builder.append("# ").append(comment);
                }
                builder.append('\n');
            }
        }

        private void addClassComments() {
            if (comments.hasClassComments()) {
                builder.append(yamlComments.classCommentsAsString());
                builder.append("\n");
            }
        }

    }
}
