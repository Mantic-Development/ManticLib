package de.exlll.configlib.yaml;

import de.exlll.configlib.Comments;
import de.exlll.configlib.ConfigurationSource;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
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

        String commentedDump = adder.getCommentedDump();
        Files.write(configPath, Arrays.asList(commentedDump.split("\n")), StandardCharsets.UTF_8);
    }

    // SnakeYAML writes a collection reached twice (one shared default, like an empty list) as an anchor and aliases, so editing
    // one of them in the file would change all of them.
    @SuppressWarnings("unchecked")
    private static <T> T unshared(T value) {
        if (value instanceof Map) {
            Map<Object, Object> copy = new LinkedHashMap<>();
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
