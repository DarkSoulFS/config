/*
 * Configurate
 * Copyright (C) zml and Configurate contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spongepowered.configurate.yaml;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.loader.CommentHandlers;
import org.spongepowered.configurate.util.UnmodifiableCollections;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.Writer;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A loader for YAML-formatted configurations, using the SnakeYAML library for
 * parsing and generation.
 *
 */
public final class YamlConfigurationLoader extends AbstractConfigurationLoader<BasicConfigurationNode> {

    /**
     * YAML native types from <a href="https://yaml.org/type/">YAML 1.1 Global tags</a>.
     *
     * <p>using SnakeYaml representation: https://bitbucket.org/asomov/snakeyaml/wiki/Documentation#markdown-header-yaml-tags-and-java-types
     */
    private static final Set<Class<?>> NATIVE_TYPES = UnmodifiableCollections.toSet(
            Boolean.class, Integer.class, Long.class, BigInteger.class, Double.class, // numeric
            byte[].class, String.class, Date.class, java.sql.Date.class, Timestamp.class, // complex types
            Set.class, List.class, Map.class); // collections

    /**
     * Creates a new {@link YamlConfigurationLoader} builder.
     *
     * @return A new builder
     */
    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds a {@link YamlConfigurationLoader}.
     */
    public static class Builder extends AbstractConfigurationLoader.Builder<Builder> {
        private final DumperOptions options = new DumperOptions();

        protected Builder() {
            setIndent(4);
            setDefaultOptions(o -> o.withNativeTypes(NATIVE_TYPES));
        }

        /**
         * Sets the level of indentation the resultant loader should use.
         *
         * @param indent The indent level
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setIndent(final int indent) {
            this.options.setIndent(indent);
            return this;
        }

        /**
         * Gets the level of indentation to be used by the resultant loader.
         *
         * @return The indent level
         */
        public int getIndent() {
            return this.options.getIndent();
        }

        /**
         * Sets the flow style the resultant loader should use.
         *
         * <p>Flow: the compact, json-like representation.<br>
         * Example: <code>
         *     {value: [list, of, elements], another: value}
         * </code></p>
         *
         * <p>Block: expanded, traditional YAML<br>
         * Example: <code>
         *     value:
         *     - list
         *     - of
         *     - elements
         *     another: value
         * </code></p>
         *
         * @param style The flow style to use
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setFlowStyle(final @NonNull FlowStyle style) {
            this.options.setDefaultFlowStyle(style);
            return this;
        }

        /**
         * Gets the flow style to be used by the resultant loader.
         *
         * @return The flow style
         */
        @NonNull
        public FlowStyle getFlowSyle() {
            return this.options.getDefaultFlowStyle();
        }

        @NonNull
        @Override
        public YamlConfigurationLoader build() {
            return new YamlConfigurationLoader(this);
        }
    }

    private final ThreadLocal<Yaml> yaml;

    private YamlConfigurationLoader(final Builder builder) {
        super(builder, new CommentHandler[] {CommentHandlers.HASH});
        final DumperOptions opts = builder.options;
        this.yaml = ThreadLocal.withInitial(() -> new Yaml(opts));
    }

    @Override
    protected void loadInternal(final BasicConfigurationNode node, final BufferedReader reader) {
        node.setValue(this.yaml.get().load(reader));
    }

    @Override
    protected void saveInternal(final ConfigurationNode node, final Writer writer) {
        this.yaml.get().dump(node.getValue(), writer);
    }

    @NonNull
    @Override
    public BasicConfigurationNode createNode(final @NonNull ConfigurationOptions options) {
        return BasicConfigurationNode.root(options);
    }

}
