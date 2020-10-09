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

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.meta.Processor;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.util.CheckedFunction;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

class ObjectMapperImpl<I, V> implements ObjectMapper<V> {

    private final Type type;
    private final List<FieldData<I, V>> fields;
    final FieldDiscoverer.InstanceFactory<I> instanceFactory;

    ObjectMapperImpl(final Type type, final List<FieldData<I, V>> fields, final FieldDiscoverer.InstanceFactory<I> instanceFactory) {
        this.type = type;
        this.fields = Collections.unmodifiableList(fields);
        this.instanceFactory = instanceFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V load(final ConfigurationNode source) throws ObjectMappingException {
        return this.load0(source, intermediate -> (V) this.instanceFactory.complete(intermediate));
    }

    final V load0(final ConfigurationNode source, final CheckedFunction<I, V, ObjectMappingException> completer) throws ObjectMappingException {
        final I intermediate = this.instanceFactory.begin();
        @MonotonicNonNull List<FieldData<I, V>> unseenFields = null;

        @Nullable ObjectMappingException failure = null;
        for (FieldData<I, V> field : this.fields) {
            try {
                final @Nullable ConfigurationNode node = field.resolveNode(source);
                if (node == null) {
                    continue;
                }

                final TypeSerializer<?> serial = field.serializerFrom(node);
                final @Nullable Object newVal = node.virtual() ? null : serial.deserialize(field.resolvedType().getType(), node);
                field.validate(newVal);

                // set up an implicit initializer
                // only the instance factory has knowledge of the underlying data type,
                // so we have to pass both implicit and explicit options along to it.
                final Supplier<@Nullable Object> implicitInitializer;
                if (newVal == null && node.options().implicitInitialization()) {
                    implicitInitializer = () -> serial.emptyValue(field.resolvedType().getType(), node.options());
                } else {
                    implicitInitializer = () -> null;
                }

                // load field into intermediate object
                field.deserializer().accept(intermediate, newVal, implicitInitializer);

                if (newVal == null && source.options().shouldCopyDefaults()) {
                    if (unseenFields == null) {
                        unseenFields = new ArrayList<>();
                    }
                    unseenFields.add(field);
                }
            } catch (final ObjectMappingException ex) {
                if (failure == null) {
                    failure = ex;
                } else {
                    failure.addSuppressed(ex);
                }
            }
        }

        if (failure != null) {
            throw failure;
        }

        final V complete = completer.apply(intermediate);
        if (unseenFields != null) {
            for (FieldData<I, V> field : unseenFields) {
                saveSingle(field, complete, source);
            }
        }
        return complete;
    }

    @Override
    public void save(final V value, final ConfigurationNode target) throws ObjectMappingException {
        for (FieldData<I, V> field : this.fields) {
            saveSingle(field, value, target);
        }

        if (target.virtual()) { // we didn't save anything
            target.set(Collections.emptyMap());
        }
    }

    @SuppressWarnings("unchecked")
    private void saveSingle(final FieldData<I, V> field, final V value, final ConfigurationNode target) throws ObjectMappingException {
        final @Nullable ConfigurationNode node = field.resolveNode(target);
        if (node == null) {
            return;
        }

        final @Nullable Object fieldVal;
        try {
            fieldVal = field.serializer().apply(value);
        } catch (final ObjectMappingException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new ObjectMappingException(ex);
        }

        if (fieldVal == null) {
            node.set(null);
        } else {
            final TypeSerializer<Object> serial = (TypeSerializer<Object>) field.serializerFrom(node);
            serial.serialize(field.resolvedType().getType(), fieldVal, node);
            for (Processor<?> processor : field.processors()) {
                ((Processor<Object>) processor).process(fieldVal, node);
            }
        }
    }

    @Override
    public List<FieldData<I, V>> fields() {
        return this.fields;
    }

    @Override
    public Type mappedType() {
        return this.type;
    }

    @Override
    public boolean canCreateInstances() {
        return this.instanceFactory.canCreateInstances();
    }

    static final class Mutable<I, V> extends ObjectMapperImpl<I, V> implements ObjectMapper.Mutable<V> {

        Mutable(final Type type, final List<FieldData<I, V>> fields, final FieldDiscoverer.MutableInstanceFactory<I> instanceFactory) {
            super(type, fields, instanceFactory);
        }

        @Override
        public void load(final V value, final ConfigurationNode node) throws ObjectMappingException {
            this.load0(node, intermediate -> {
                ((FieldDiscoverer.MutableInstanceFactory<I>) this.instanceFactory).complete(value, intermediate);
                return value;
            });
        }
    }

}
