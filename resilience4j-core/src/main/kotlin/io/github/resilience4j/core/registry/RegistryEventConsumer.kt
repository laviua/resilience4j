package io.github.resilience4j.core.registry

interface RegistryEventConsumer<E> {
    fun onEntryAddedEvent(entryAddedEvent: EntryAddedEvent<E>)
    fun onEntryRemovedEvent(entryRemoveEvent: EntryRemovedEvent<E>)
    fun onEntryReplacedEvent(entryReplacedEvent: EntryReplacedEvent<E>)
}
