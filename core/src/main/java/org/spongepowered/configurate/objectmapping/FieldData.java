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
package org.spongepowered.configurate.objectmapping;

import static io.leangen.geantyref.GenericTypeReflector.box;
import static io.leangen.geantyref.GenericTypeReflector.erase;

import com.google.auto.value.AutoValue;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.meta.Constraint;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.objectmapping.meta.Processor;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.util.CheckedFunction;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.function.Supplier;

/**
 * Holder for field-specific information.
 *
 * @param <I> intermediate type
 * @param <O> container type
 * @since 4.0.0
 */
@AutoValue
@SuppressWarnings("AutoValueImmutableFields") // we don't use guava collections
public abstract class FieldData<I, O> {

    /**
     * Create a new data holder for a field.
     *
     * @param name field name
     * @param resolvedFieldType type resolved for specific data
     * @param constraints constraints to verify on this field
     * @param processors processors to apply to fields backing this node
     * @param <I> intermediate type
     * @param <O> container type
     * @return new field data
     * @since 4.0.0
     */
    static <I, O> FieldData<I, O> of(final String name, final AnnotatedType resolvedFieldType,
            final List<Constraint<?>> constraints, final List<Processor<?>> processors,
            final Deserializer<I> deserializer, final CheckedFunction<O, @Nullable Object, Exception> serializer, final NodeResolver resolver) {
        return new AutoValue_FieldData<>(name, resolvedFieldType,
                UnmodifiableCollections.copyOf(constraints),
                UnmodifiableCollections.copyOf(processors),
                deserializer, serializer, resolver);
    }

    FieldData() {
    }

    /**
     * The name of the field.
     *
     * @return field name
     * @since 4.0.0
     */
    public abstract String name();

    /**
     * The calculated type of this field within the object type.
     *
     * <p>This value has had any possible type parameters resolved using
     * information available in the context.</p>
     *
     * @return the resolved type
     * @since 4.0.0
     */
    public abstract AnnotatedType resolvedType();

    abstract List<Constraint<?>> constraints();

    abstract List<Processor<?>> processors();

    abstract Deserializer<I> deserializer();

    abstract CheckedFunction<O, @Nullable Object, Exception> serializer();

    abstract NodeResolver nodeResolver();

    /**
     * Test if an object would be valid for this field.
     *
     * @param instance instance to validate
     * @return true if valid
     * @since 4.0.0
     */
    public boolean isValid(final Object instance) {
        try {
            validate(instance);
            return true;
        } catch (final SerializationException ex) {
            return false;
        }
    }

    /**
     * Try to ensure the provided value is acceptable.
     *
     * @param instance field value instance to validate
     * @throws SerializationException if validation fails
     * @since 4.0.0
     */
    @SuppressWarnings("unchecked")
    public void validate(final @Nullable Object instance) throws SerializationException {
        if (instance != null && !erase(box(resolvedType().getType())).isInstance(instance)) {
            throw new SerializationException("Object " + instance + " is not of expected type " + resolvedType().getType());
        }

        for (final Constraint<?> constraint : constraints()) {
            ((Constraint<Object>) constraint).validate(instance);
        }
    }

    TypeSerializer<?> serializerFrom(final ConfigurationNode node) throws SerializationException {
        final @Nullable TypeSerializer<?> serial = node.options().serializers().get(resolvedType());
        if (serial == null) {
            throw new SerializationException("No TypeSerializer found for field " + name() + " of type " + resolvedType().getType());
        }
        return serial;
    }

    /**
     * Use this field's node resolvers to determine a target node.
     *
     * @param source parent node
     * @return resolved node
     * @since 4.0.0
     */
    public @Nullable ConfigurationNode resolveNode(final ConfigurationNode source) {
        return this.nodeResolver().resolve(source);
    }

    /**
     * A deserialization handler to appropriately place object data into fields.
     *
     * @param <I> intermediate data type
     * @since 4.0.0
     */
    @FunctionalInterface
    public interface Deserializer<I> {

        /**
         * Apply either a new value or implicit initializer to the
         * {@code intermediate} object as appropriate.
         *
         * @param intermediate the intermediate container
         * @param newValue new value to store
         * @param implicitInitializer the implicit initializer
         * @since 4.0.0
         */
        void accept(I intermediate, @Nullable Object newValue, Supplier<@Nullable Object> implicitInitializer);
    }

}
