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
package org.spongepowered.configurate.reference;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.reactive.Publisher;

import java.util.function.Function;

/**
 * A pointer to a node within a configuration tree.
 *
 * <p>This value will update automatically with changes to the underlying
 * configuration file. Subscribers will be provided the current value upon
 * subscription, followed by any changes.
 *
 * @param <T> the type of value to return
 * @param <N> the type of node
 */
public interface ValueReference<T, N extends ConfigurationNode> extends Publisher<T> {

    /**
     * Get the current value at this node.
     *
     * <p>Any deserialization failures will be submitted to the owning
     * {@link ConfigurationReference}'s error callback
     *
     * @return the deserialized value, or null if deserialization fails.
     */
    @Nullable T get();

    /**
     * Set the new value of this node. The configuration won't be saved.
     *
     * <p>Any serialization errors will be provided to the error callback of
     * the owning {@link ConfigurationReference}
     *
     * @param value the value
     * @return true if successful, false if serialization fails
     */
    boolean set(@Nullable T value);

    /**
     * Set the new value of this node and save the underlying configuration.
     *
     * <p>Any serialization errors will be provided to the error callback of the
     * owning {@link ConfigurationReference}
     *
     * @param value the value
     * @return true if successful, false if serialization fails
     */
    boolean setAndSave(@Nullable T value); // @cs-: NoGetSetPrefix (not a property accessor)

    /**
     * Set the new value of this node and save the underlying configuration
     * asynchronously on the executor of the owning {@link ConfigurationReference}.
     *
     * <p>Any serialization errors will be submitted to subscribers of the
     * returned {@link Publisher}
     *
     * @param value the value
     * @return true if successful, false if serialization fails
     */
    Publisher<Boolean> setAndSaveAsync(@Nullable T value); // @cs-: NoGetSetPrefix (not a property accessor)

    /**
     * Update this value and the underlying node, without saving.
     *
     * <p>Any serialization errors will be provided to the error callback of
     * the owning {@link ConfigurationReference}
     *
     * @param action to transform this node's value
     * @return whether this update was successful
     */
    boolean update(Function<@Nullable T, ? extends T> action);

    /**
     * Update, performing the action and save on the executor of the owning
     * {@link ConfigurationReference}. Any errors that occur while saving will
     * be passed along to any subscribers.
     *
     * <p>The updated value will only be exposed if the changes are successful.
     *
     * @param action to transform this node's value
     * @return whether this update was successful
     */
    Publisher<Boolean> updateAsync(Function<T, ? extends T> action);

    /**
     * Get the node this value reference points to.
     *
     * @return the node
     */
    N node();

}
