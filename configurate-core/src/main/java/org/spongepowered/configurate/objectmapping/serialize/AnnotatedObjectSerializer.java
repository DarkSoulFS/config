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
package org.spongepowered.configurate.objectmapping.serialize;

import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.lang.reflect.Modifier;

class AnnotatedObjectSerializer implements TypeSerializer<Object> {
    @Override
    public <Node extends ConfigurationNode<Node>> Object deserialize(@NonNull TypeToken<?> type, @NonNull Node value) throws ObjectMappingException {
        TypeToken<?> clazz = getInstantiableType(type, value.getNode("__class__").getString());
        return value.getOptions().getObjectMapperFactory().getMapper(clazz).bindToNew().populate(value);
    }

    private TypeToken<?> getInstantiableType(TypeToken<?> type, String configuredName) throws ObjectMappingException {
        TypeToken<?> retClass = type;
        if (type.getRawType().isInterface() || Modifier.isAbstract(type.getRawType().getModifiers())) {
            if (configuredName == null) {
                throw new ObjectMappingException("No available configured type for instances of " + type);
            } else {
                try {
                    retClass = TypeToken.of(Class.forName(configuredName));
                } catch (ClassNotFoundException e) {
                    throw new ObjectMappingException("Unknown class of object " + configuredName, e);
                }
                if (!type.isSupertypeOf(retClass)) {
                    throw new ObjectMappingException("Configured type " + configuredName + " does not extend "
                            + type.getRawType().getCanonicalName());
                }
            }
        }
        return retClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ConfigurationNode<T>> void serialize(@NonNull TypeToken<?> type, @Nullable Object obj, @NonNull T value) throws ObjectMappingException {
        if (type.getRawType().isInterface() || Modifier.isAbstract(type.getRawType().getModifiers())) {
            // serialize obj's concrete type rather than the interface/abstract class
            value.getNode("__class__").setValue(obj.getClass().getName());
        }
        ((ObjectMapper<Object>) value.getOptions().getObjectMapperFactory().getMapper(type)).bind(obj).serialize(value);
    }
}
