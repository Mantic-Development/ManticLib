package de.exlll.configlib.yaml;

import de.exlll.configlib.CommentedMap;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;

import java.io.StringReader;
import java.util.*;

/** Uses parser positions so quoted keys, sequences and scalar contents stay intact. */
final class YamlFieldComments {
    static String add(String dump, Object data, Yaml yaml) {
        Map<Integer, String> insertions = new TreeMap<>(Collections.reverseOrder());
        Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<Node, Boolean>());
        collect(yaml.compose(new StringReader(dump)), data, dump, insertions, visited);
        StringBuilder result = new StringBuilder(dump);
        insertions.forEach(result::insert);
        return result.toString();
    }

    private static void collect(Node node, Object data, String dump,
                                Map<Integer, String> insertions, Set<Node> visited) {
        // Aliases share a node and its original source position; annotate it only once.
        if (node == null || !visited.add(node)) return;
        if (node instanceof MappingNode && data instanceof Map) {
            Iterator<? extends Map.Entry<?, ?>> entries = ((Map<?, ?>) data).entrySet().iterator();
            // SnakeYAML represents maps in their iteration order, including non-string keys.
            boolean firstEntry = true;
            for (NodeTuple tuple : ((MappingNode) node).getValue()) {
                if (!entries.hasNext()) break;
                Map.Entry<?, ?> entry = entries.next();
                if (data instanceof CommentedMap) {
                    List<String> comments = ((CommentedMap) data).getFieldComments().get(entry.getKey());
                    if (comments != null && !comments.isEmpty()) {
                        addAt(tuple.getKeyNode().getStartMark(), comments, dump, insertions,
                                firstEntry && comments.get(0).isEmpty());
                    }
                }
                collect(tuple.getValueNode(), entry.getValue(), dump, insertions, visited);
                firstEntry = false;
            }
        } else if (node instanceof SequenceNode && data instanceof Iterable) {
            Iterator<?> elements = ((Iterable<?>) data).iterator();
            for (Node child : ((SequenceNode) node).getValue()) {
                if (!elements.hasNext()) break;
                collect(child, elements.next(), dump, insertions, visited);
            }
        } else if (node instanceof MappingNode && data instanceof Set) {
            // YAML sets store their members as mapping keys with null values.
            Iterator<?> elements = ((Set<?>) data).iterator();
            for (NodeTuple tuple : ((MappingNode) node).getValue()) {
                if (!elements.hasNext()) break;
                collect(tuple.getKeyNode(), elements.next(), dump, insertions, visited);
            }
        }
    }

    private static void addAt(Mark mark, List<String> comments, String dump,
                              Map<Integer, String> insertions, boolean moveBeforeSequenceDash) {
        int offset = dump.offsetByCodePoints(0, mark.getIndex());
        String indent = String.join("", Collections.nCopies(mark.getColumn(), " "));
        if (moveBeforeSequenceDash) {
            int lineStart = dump.lastIndexOf('\n', offset - 1) + 1;
            if (dump.substring(lineStart, offset).trim().equals("-")) {
                int dash = dump.indexOf('-', lineStart);
                indent = dump.substring(lineStart, dash);
                offset = dash;
            } else if (lineStart > 0) {
                int previousLineStart = dump.lastIndexOf('\n', lineStart - 2) + 1;
                if (dump.substring(previousLineStart, lineStart).trim().equals("-")) {
                    int dash = dump.indexOf('-', previousLineStart);
                    indent = dump.substring(previousLineStart, dash);
                    offset = dash;
                }
            }
        }
        StringBuilder text = new StringBuilder();
        // Flow mappings may start immediately after '{' or ','. A comment needs separation.
        if (offset > 0 && !Character.isWhitespace(dump.charAt(offset - 1))) text.append(' ');
        for (String comment : comments) {
            for (String line : comment.split("\\r\\n|\\r|\\n", -1)) {
                if (!line.isEmpty()) text.append("# ").append(line);
                text.append('\n').append(indent);
            }
        }
        insertions.put(offset, text.toString());
    }
}
