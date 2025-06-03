package io.github.resilience4j.core

import io.github.resilience4j.core.registry.EntryAddedEvent
import io.github.resilience4j.core.registry.EntryRemovedEvent
import io.github.resilience4j.core.registry.EntryReplacedEvent
import io.github.resilience4j.core.registry.RegistryEvent

interface Registry<E, C> {
    fun addConfiguration(configName: String, configuration: C)

    fun find(name: String): E?

    fun remove(name: String): E?

    fun replace(name: String, newEntry: E): E?

    fun getConfiguration(configName: String): C?

    fun getDefaultConfig(): C

    fun getTags(): Map<String, String>

    fun getEventPublisher(): EventPublisher<E>

    interface EventPublisher<E> : io.github.resilience4j.core.EventPublisher<RegistryEvent> {
        fun onEntryAdded(eventConsumer: EventConsumer<EntryAddedEvent<E>>): EventPublisher<E>
        fun onEntryRemoved(eventConsumer: EventConsumer<EntryRemovedEvent<E>>): EventPublisher<E>
        fun onEntryReplaced(eventConsumer: EventConsumer<EntryReplacedEvent<E>>): EventPublisher<E>
    }
}
