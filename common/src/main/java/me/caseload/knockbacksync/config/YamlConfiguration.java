package me.caseload.knockbacksync.config;

import lombok.Getter;
import lombok.Setter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class YamlConfiguration {
    private final File file;
    private final Yaml yaml;
    @Getter
    @Setter
    private Map<String, Object> data;
    private String fileContents;

    public YamlConfiguration(File file) {
        this.file = file;
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setWidth(Integer.MAX_VALUE);
        this.yaml = new Yaml(options);
    }

    public void load() throws IOException {
        if (!file.exists()) {
            data = new LinkedHashMap<>();
            return;
        }

        fileContents = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        data = yaml.load(fileContents);
        if (data == null) {
            data = new LinkedHashMap<>();
        }
    }

    public void save() throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        String dump = yaml.dump(data);

        if (fileContents != null) {
            Map<String, YamlElement> oldElements = parseYaml(fileContents);
            Map<String, YamlElement> newElements = parseYaml(dump);

            StringBuilder result = new StringBuilder();
            buildOutput(oldElements, newElements, result);
            dump = result.toString();
        }

        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
            writer.write(dump);
        }
    }

    private static class YamlElement {
        String key;
        String value;
        int indent;
        boolean isComment;
        List<YamlElement> children = new ArrayList<>();

        YamlElement(String line, int indent) {
            this.indent = indent;
            if (line.trim().startsWith("#")) {
                this.isComment = true;
                this.value = line;
            } else {
                this.isComment = false;
                String[] parts = line.split(":", 2);
                this.key = parts[0].trim();
                this.value = parts.length > 1 ? parts[1].trim() : "";
            }
        }
    }

    private Map<String, YamlElement> parseYaml(String content) {
        Map<String, YamlElement> elements = new LinkedHashMap<>();
        Stack<YamlElement> stack = new Stack<>();
        String[] lines = content.split("\n");

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            int indent = getIndentation(line);
            YamlElement element = new YamlElement(line, indent);

            while (!stack.isEmpty() && stack.peek().indent >= indent) {
                stack.pop();
            }

            if (stack.isEmpty()) {
                if (!element.isComment) {
                    elements.put(element.key, element);
                } else {
                    elements.put("comment" + elements.size(), element);
                }
            } else {
                stack.peek().children.add(element);
            }

            if (!element.isComment && element.value.isEmpty()) {
                stack.push(element);
            }
        }

        return elements;
    }

    private void buildOutput(Map<String, YamlElement> oldElements, Map<String, YamlElement> newElements, StringBuilder result) {
        for (YamlElement element : oldElements.values()) {
            if (element.isComment) {
                result.append(element.value).append("\n");
            } else {
                YamlElement newElement = newElements.get(element.key);
                if (newElement != null) {
                    String indent = createIndent(element.indent);
                    result.append(indent).append(element.key).append(":");

                    if (!newElement.value.isEmpty()) {
                        result.append(" ").append(newElement.value);
                    }
                    result.append("\n");

                    if (!element.children.isEmpty()) {
                        Map<String, YamlElement> oldChildren = new LinkedHashMap<>();
                        Map<String, YamlElement> newChildren = new LinkedHashMap<>();

                        for (YamlElement child : element.children) {
                            if (!child.isComment) {
                                oldChildren.put(child.key, child);
                            } else {
                                oldChildren.put("comment" + oldChildren.size(), child);
                            }
                        }

                        for (YamlElement child : newElement.children) {
                            if (!child.isComment) {
                                newChildren.put(child.key, child);
                            }
                        }

                        buildOutput(oldChildren, newChildren, result);
                    }
                }
            }
        }
    }

    private int getIndentation(String line) {
        int indent = 0;
        while (indent < line.length() && line.charAt(indent) == ' ') {
            indent++;
        }
        return indent;
    }

    private String createIndent(int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }
}