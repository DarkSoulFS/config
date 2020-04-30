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
package org.spongepowered.configurate.reactive;

/**
 * A subscriber that is transaction-aware. As opposed to standard Subscribers which receive simple
 * value events, transactional subscribers receive a series of events: first, a {@code
 * beginTransaction}, followed by a {@code commit} or {@code rollback}.
 *
 * @param <V> The value handled by this subscriber
 */
public interface TransactionalSubscriber<V> extends Subscriber<V> {
    @Override
    default void submit(V item) {
        try {
            beginTransaction(item);
            commit();
        } catch (TransactionFailedException ex) {
            rollback();
        }
    }

    /**
     * Receive a new value, and validate it. The received value must not be made available outside
     * of to other transaction-aware viewers until {@link #commit()} has been called.
     *
     * @param newValue The new value
     * @throws TransactionFailedException if the new value does not validate
     */
    void beginTransaction(V newValue) throws TransactionFailedException;

    /**
     * Expose a transaction's result
     * <p>
     * This method will be called on all transactional subscribers in a system have received and
     * validated any new data. Calling this method when a transaction is not in progress should
     * result in a noop.
     */
    void commit();

    /**
     * Called when a transaction has failed, to revert any prepared changes
     * <p>
     * This event indicates that it is safe for clients to discard any prepared information from an
     * in-progress transaction. If there is no transaction in progress, this must be a no-op.
     */
    void rollback();
}
