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

import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.transformation.NodePath;
import org.spongepowered.configurate.reactive.Publisher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

/**
 * An updating reference to a base configuration node
 *
 * @param <N> The type of node to work with
 */
public interface ConfigurationReference<N extends ConfigurationNode> extends AutoCloseable {
    /**
     * Create a new configuration reference that will only update when loaded
     *
     * @param loader The loader to load and save from
     * @param <T> The type of node
     * @return The newly created reference, with an initial load performed
     * @throws IOException If the configuration contained fails to load
     */
    static <T extends ScopedConfigurationNode<T>> ConfigurationReference<T> createFixed(ConfigurationLoader<T> loader) throws IOException {
        ConfigurationReference<T> ret = new ManualConfigurationReference<>(loader);
        ret.load();
        return ret;
    }

    /**
     * Create a new configuration reference that will automatically update when triggered by the provided {@link WatchServiceListener}
     *
     * @param loaderCreator A function that can create a {@link ConfigurationLoader}
     * @param file The file to load this configuration from
     * @param listener The watch service listener that will receive events
     * @param <T> The node type
     * @return The created reference
     * @throws IOException If the underlying loader fails to load a configuration
     * @see WatchServiceListener#listenToConfiguration(Function, Path)
     */
    static <T extends ScopedConfigurationNode<T>> ConfigurationReference<T> createWatching(Function<Path, ConfigurationLoader<T>> loaderCreator, Path file, WatchServiceListener listener) throws IOException {
        final WatchingConfigurationReference<T> ret = new WatchingConfigurationReference<>(loaderCreator.apply(file));
        ret.load();
        ret.setDisposable(listener.listenToFile(file, ret));

        return ret;
    }

    /**
     * Reload a configuration using the provided loader.
     *
     * If the load fails, this reference will continue pointing to old configuration values
     *
     * @throws IOException When an error occurs
     */
    void load() throws IOException;

    /**
     * Save this configuration using the provided loader.
     *
     * @throws IOException When an error occurs in the underlying ID
     */
    void save() throws IOException;

    /**
     * Update the configuration node pointed to by this reference, and save it using the reference's loader
     *
     * Even if the loader fails to save this new node, the node pointed to by this reference will be updated.
     *
     * @param newNode The new node to save
     * @throws IOException When an error occurs within the loader
     */
    void save(N newNode) throws IOException;

    /**
     * Get the base node this reference refers to.
     *
     * @return The node
     */
    N getNode();

    /**
     * Get the loader this reference uses to load and save its' node
     *
     * @return The loader
     */
    ConfigurationLoader<N> getLoader();

    /**
     * Get the node at the given path, using the root node
     *
     * @param path The path, a series of path elements
     * @return A child node
     * @see ConfigurationNode#getNode(Object...)
     */
    N get(Object... path);

    /**
     * Update the value of the node at the given path, using the root node as a base.
     *
     * @param path The path to get the child at
     * @param value The value to set the child node to
     */
    default void set(Object[] path, @Nullable Object value) {
        getNode().getNode(path).setValue(value);
    }

    /**
     * Set the value of the node at {@code path} to the given value,
     * using the appropriate {@link org.spongepowered.configurate.objectmapping.serialize.TypeSerializer} to serialize the data if it's
     * not directly supported by the provided configuration.
     *
     * @param path The path to set the value at
     * @param type The type of data to serialize
     * @param value The value to set
     * @param <T> The type parameter for the value
     * @throws ObjectMappingException If thrown by the serialization mechanism
     */
    default <T> void set(Object[] path, TypeToken<T> type, @Nullable T value) throws ObjectMappingException {
        getNode().getNode(path).setValue(type, value);
    }

    /**
     * Update the value of the node at the given path, using the root node as a base.
     *
     * @param path The path to get the child at
     * @param value The value to set the child node to
     */
    default void set(NodePath path, @Nullable Object value) {
        getNode().getNode(path).setValue(value);
    }

    /**
     * Set the value of the node at {@code path} to the given value,
     * using the appropriate {@link org.spongepowered.configurate.objectmapping.serialize.TypeSerializer} to
     * serialize the data if it's not directly supported by the provided configuration.
     *
     * @param path The path to set the value at
     * @param type The type of data to serialize
     * @param value The value to set
     * @param <T> The type parameter for the value
     * @throws ObjectMappingException If thrown by the serialization mechanism
     */
    default <T> void set(NodePath path, TypeToken<T> type, @Nullable T value) throws ObjectMappingException {
        getNode().getNode(path).setValue(type, value);
    }

    /**
     * Create a reference to the node at the provided path. The value will be deserialized according to the provided
     * TypeToken.
     *
     * The returned reference will update with reloads of and changes to the value of the provided configuration.
     * Any serialization errors encountered will be submitted to the {@link #errors()} stream
     *
     * @param type The value's type
     * @param path The path from the root node to the node a value will be gotten from
     * @param <T> The value type
     * @return A deserializing reference to the node at the given path
     * @throws ObjectMappingException if a type serializer could not be found for the provided type
     */
    default <T> ValueReference<T, N> referenceTo(TypeToken<T> type, Object... path) throws ObjectMappingException {
        return referenceTo(type, NodePath.create(path));
    }

    /**
     * Create a reference to the node at the provided path. The value will be deserialized according to type of the
     * provided Class.
     *
     * The returned reference will update with reloads of and changes to the value of the provided configuration.
     * Any serialization errors encountered will be submitted to the {@link #errors()} stream
     *
     * @param type The value's type
     * @param path The path from the root node to the node a value will be gotten from
     * @param <T> The value type
     * @return A deserializing reference to the node at the given path
     * @throws ObjectMappingException if a type serializer could not be found for the provided type
     */
    default <T> ValueReference<T, N> referenceTo(Class<T> type, Object... path) throws ObjectMappingException {
        return referenceTo(type, NodePath.create(path));
    }

    /**
     * Create a reference to the node at the provided path. The value will be deserialized according to the provided
     * TypeToken.
     *
     * The returned reference will update with reloads of and changes to the value of the provided configuration.
     * Any serialization errors encountered will be submitted to the {@link #errors()} stream
     *
     * @param type The value's type
     * @param path The path from the root node to the node a value will be gotten from
     * @param <T> The value type
     * @return A deserializing reference to the node at the given path
     * @throws ObjectMappingException if a type serializer could not be found for the provided type
     */
    <T> ValueReference<T, N> referenceTo(TypeToken<T> type, NodePath path) throws ObjectMappingException;

    /**
     * Create a reference to the node at the provided path. The value will be deserialized according to type of the
     * provided Class.
     *
     * The returned reference will update with reloads of and changes to the value of the provided configuration.
     * Any serialization errors encountered will be submitted to the {@link #errors()} stream
     *
     * @param type The value's type
     * @param path The path from the root node to the node a value will be gotten from
     * @param <T> The value type
     * @return A deserializing reference to the node at the given path
     * @throws ObjectMappingException if a type serializer could not be found for the provided type
     */
    <T> ValueReference<T, N> referenceTo(Class<T> type, NodePath path) throws ObjectMappingException;

    /**
     * Access the {@link Publisher} that will broadcast update events, providing the newly created node
     *
     * @return The publisher
     */
    Publisher<N> updates();

    /**
     * A stream that will receive errors that occur while loading or saving to this reference
     *
     * @return The publisher
     */
    Publisher<Map.Entry<ErrorPhase, Throwable>> errors();

    /**
     * {@inheritDoc}
     */
    @Override
    void close();

    /**
     * Representing the phase where an error occurred
     */
    enum ErrorPhase {
        LOADING, SAVING, UNKNOWN;
    }
}
