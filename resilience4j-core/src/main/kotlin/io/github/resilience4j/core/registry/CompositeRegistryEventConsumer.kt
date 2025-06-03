package io.github.resilience4j.core.registry

class CompositeRegistryEventConsumer<E>(delegates: List<RegistryEventConsumer<E>>) : RegistryEventConsumer<E> {
    private val delegates: List<RegistryEventConsumer<E>> = ArrayList(requireNotNull(delegates))

    override fun onEntryAddedEvent(entryAddedEvent: EntryAddedEvent<E>) {
        delegates.forEach { it.onEntryAddedEvent(entryAddedEvent) }
    }

    override fun onEntryRemovedEvent(entryRemoveEvent: EntryRemovedEvent<E>) {
        delegates.forEach { it.onEntryRemovedEvent(entryRemoveEvent) }
    }

    override fun onEntryReplacedEvent(entryReplacedEvent: EntryReplacedEvent<E>) {
        delegates.forEach { it.onEntryReplacedEvent(entryReplacedEvent) }
    }
}
