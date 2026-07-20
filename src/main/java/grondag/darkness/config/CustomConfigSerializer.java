/*
 * This file is part of True Darkness and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.darkness.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.Toml;
import me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.TomlWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CustomConfigSerializer<T extends ConfigData> implements ConfigSerializer<T> {
    private final Path filePath;
    private final Class<T> configClass;
    private final TomlWriter tomlWriter = new TomlWriter();

    public CustomConfigSerializer(Class<T> configClass, Path filePath) {
        this.configClass = configClass;
        this.filePath = filePath;
    }

    @Override
    public void serialize(T config) throws SerializationException {
        try {
            // 1. generate a standard TOML file without comments
            String rawToml = tomlWriter.write(config);

            // 2. Collect all field comments (path -> comment content)
            Map<String, String> commentMap = new HashMap<>();
            collectComments(configClass, "", commentMap);

            // 3. Insert the comments
            String tomlWithComments = insertComments(rawToml, commentMap);

            // 4. Write to the file
            Path configPath = getConfigPath();
            Files.createDirectories(configPath.getParent());
            Files.write(configPath, tomlWithComments.getBytes());
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

    /**
     * Recursively collect all fields' @TomlComment, generating the full path (dot-separated).
     */
    private void collectComments(@NotNull Class<?> clazz, String prefix, Map<String, String> commentMap) {
        for (Field field : clazz.getDeclaredFields()) {
            String fullPath = prefix.isEmpty() ? field.getName() : prefix + "." + field.getName();
            TomlComment annotation = field.getAnnotation(TomlComment.class);
            if (annotation != null) {
                commentMap.put(fullPath, annotation.value());
            }

            //If the field is a custom type (not a basic type, string, enum, or collection), handle it recursively.
            Class<?> type = field.getType();
            if (!type.isPrimitive() && !type.isEnum() && !type.equals(String.class)
                    && !Collection.class.isAssignableFrom(type)
                    && !Map.class.isAssignableFrom(type)
                    && !type.isArray()) {
                collectComments(type, fullPath, commentMap);
            }
        }
    }

    // Insert a comment line in a standard TOML string.
    private String insertComments(String rawToml, Map<String, String> commentMap) {
        String[] lines = rawToml.split("\n");
        StringBuilder sb = new StringBuilder();
        String currentTable = "";

        for (String line : lines) {
            String trimmed = line.trim();

            // Keep empty string.
            if (trimmed.isEmpty()) {
                sb.append(line).append("\n");
                continue;
            }

            // Handle the table header [xxx] or [xxx.yyy]
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                String tablePath = trimmed.substring(1, trimmed.length() - 1).trim();
                currentTable = tablePath;
                // If the table path has comments, insert the comments before the table header.
                if (commentMap.containsKey(tablePath)) {
                    sb.append(commentMap.get(tablePath)).append("\n");
                }
                sb.append(line).append("\n");
                continue;
            }

            // Handle the key-value pair (key = value)
            int eqIndex = trimmed.indexOf('=');
            if (eqIndex > 0) {
                String key = trimmed.substring(0, eqIndex).trim();
                String fullKey = currentTable.isEmpty() ? key : currentTable + "." + key;
                if (commentMap.containsKey(fullKey)) {
                    sb.append(commentMap.get(fullKey)).append("\n");
                }
                sb.append(line).append("\n\n");
                continue;
            }

            // Keep other string(Such as Array, Multiline string, etc.).
            sb.append(line).append("\n");
        }

        return sb.toString();
    }

    @Override
    public T deserialize() throws SerializationException {
        Path configPath = getConfigPath();
        if (Files.exists(configPath)) {
            try {
                return new Toml().read(configPath.toFile()).to(this.configClass);
            } catch (IllegalStateException e) {
                throw new SerializationException(e);
            }
        } else {
            return createDefault();
        }
    }

    @Override
    public T createDefault() {
        return Utils.constructUnsafely(this.configClass);
    }

    private Path getConfigPath() {
        return filePath;
    }
}