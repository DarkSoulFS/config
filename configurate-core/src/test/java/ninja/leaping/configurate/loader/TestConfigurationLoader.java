/*
 * This file is part of Configurate, licensed under the Apache-2.0 License.
 *
 * Copyright (C) zml
 * Copyright (C) IchorPowered
 * Copyright (C) Contributors
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

package ninja.leaping.configurate.loader;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.SimpleConfigurationNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

/**
 * This test configuration loader holds a single {@link ConfigurationNode}, {@code result}, that is updated when a node is saved and loaded when necessary.
 */
public class TestConfigurationLoader extends AbstractConfigurationLoader<ConfigurationNode> {
    private ConfigurationNode result;
    public static final class Builder extends AbstractConfigurationLoader.Builder<Builder> {

        @Override
        public TestConfigurationLoader build() {
            return new TestConfigurationLoader(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    protected TestConfigurationLoader(Builder builder) {
        super(builder, CommentHandlers.values());
    }

    @Override
    protected void loadInternal(ConfigurationNode node, BufferedReader reader) throws IOException {
        node.setValue(result);
    }

    @Override
    protected void saveInternal(ConfigurationNode node, Writer writer) throws IOException {
        result.setValue(node);
    }

    public ConfigurationNode getNode() {
        return this.result;
    }

    public void setNode(ConfigurationNode node) {
        this.result = node;
    }

    /**
     * Return an empty node of the most appropriate type for this loader
     *
     * @param options The options to use with this node. Must not be null (take a look at {@link ConfigurationOptions#defaults()})
     * @return The appropriate node type
     */
    @Override
    public ConfigurationNode createEmptyNode(ConfigurationOptions options) {
        return SimpleConfigurationNode.root(options);
    }
}
