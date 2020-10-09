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
package org.spongepowered.configurate;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Iterator;

/**
 * The value in a {@link ConfigurationNode}.
 */
abstract class ConfigValue<N extends ScopedConfigurationNode<N>, T extends AbstractConfigurationNode<N, T>> {

    /**
     * The node this value "belongs" to.
     */
    protected final @NonNull T holder;

    protected ConfigValue(final @NonNull T holder) {
        this.holder = holder;
    }

    /**
     * Gets the value encapsulated by this instance.
     *
     * @return the value
     */
    abstract @Nullable Object get();

    /**
     * Sets the value encapsulated by this instance.
     *
     * @param value the value
     */
    abstract void set(@Nullable Object value);

    /**
     * Put a child value, or null to remove value at that key.
     *
     * @param key the key
     * @param value the node to put at key
     * @return existing node at key, if present
     */
    @Nullable
    abstract T putChild(@NonNull Object key, @Nullable T value);

    /**
     * Put a child value, if one isn't already present at that key.
     *
     * @param key the key
     * @param value the node to put at key
     * @return existing node at key, if present
     */
    @Nullable
    abstract T putChildIfAbsent(@NonNull Object key, @Nullable T value);

    /**
     * Gets the currently present child for the given key. Returns null if no
     * child is present.
     *
     * @param key the key to get child at
     * @return the child if any
     */
    @Nullable
    abstract T child(@Nullable Object key);

    /**
     * Returns an iterable over all child nodes.
     *
     * @return an iterator
     */
    @NonNull
    abstract Iterable<T> iterateChildren();

    /**
     * Creates a copy of this node.
     *
     * @return a copy
     */
    @NonNull
    abstract ConfigValue<N, T> copy(@NonNull T holder);

    /**
     * Whether this value has any content.
     *
     * @return value
     */
    abstract boolean isEmpty();

    /**
     * Clears the set value (or any attached child values) from this value.
     */
    void clear() {
        for (Iterator<T> it = iterateChildren().iterator(); it.hasNext();) {
            final T node = it.next();
            node.attached = false;
            it.remove();
            if (node.parentEnsureAttached().equals(this.holder)) {
                node.clear();
            }
        }
    }

}
