package io.github.resilience4j.core.registry

import io.github.resilience4j.core.EventConsumer
import io.github.resilience4j.core.EventProcessor
import io.github.resilience4j.core.Registry
import io.github.resilience4j.core.RegistryStore
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.Supplier

open class AbstractRegistry<E, C> : Registry<E, C> {
    protected val entryMap: RegistryStore<E>
    protected val configurations: ConcurrentMap<String, C>
    protected val registryTags: Map<String, String>
    private val eventProcessor: RegistryEventProcessor

    constructor(defaultConfig: C) : this(defaultConfig, emptyList(), emptyMap())

    constructor(defaultConfig: C, registryTags: Map<String, String>) : this(defaultConfig, emptyList(), registryTags)

    constructor(defaultConfig: C, registryEventConsumer: RegistryEventConsumer<E>) :
        this(defaultConfig, listOf(registryEventConsumer), emptyMap())

    constructor(
        defaultConfig: C,
        registryEventConsumer: RegistryEventConsumer<E>,
        tags: Map<String, String>
    ) : this(defaultConfig, listOf(registryEventConsumer), tags)

    constructor(defaultConfig: C, registryEventConsumers: List<RegistryEventConsumer<E>>) :
        this(defaultConfig, registryEventConsumers, emptyMap())

    constructor(
        defaultConfig: C,
        registryEventConsumers: List<RegistryEventConsumer<E>>,
        tags: Map<String, String>
    ) : this(defaultConfig, registryEventConsumers, tags, InMemoryRegistryStore())

    constructor(
        defaultConfig: C,
        registryEventConsumers: List<RegistryEventConsumer<E>>,
        tags: Map<String, String>,
        registryStore: RegistryStore<E>
    ) {
        configurations = ConcurrentHashMap()
        entryMap = Objects.requireNonNull(registryStore, REGISTRY_STORE_MUST_NOT_BE_NULL)
        eventProcessor = RegistryEventProcessor(Objects.requireNonNull(registryEventConsumers, CONSUMER_MUST_NOT_BE_NULL))
        registryTags = Objects.requireNonNull(tags, TAGS_MUST_NOT_BE_NULL)
        configurations[DEFAULT_CONFIG] = Objects.requireNonNull(defaultConfig, CONFIG_MUST_NOT_BE_NULL)
    }

    protected fun computeIfAbsent(name: String, supplier: Supplier<E>): E =
        entryMap.computeIfAbsent(Objects.requireNonNull(name, NAME_MUST_NOT_BE_NULL)) {
            val entry = supplier.get()
            eventProcessor.processEvent(EntryAddedEvent(entry))
            entry
        }

    override fun find(name: String): E? = entryMap.find(name)

    override fun remove(name: String): E? {
        val removedEntry = entryMap.remove(name)
        removedEntry?.let { eventProcessor.processEvent(EntryRemovedEvent(it)) }
        return removedEntry
    }

    override fun replace(name: String, newEntry: E): E? {
        val replacedEntry = entryMap.replace(name, newEntry)
        replacedEntry?.let { eventProcessor.processEvent(EntryReplacedEvent(it, newEntry)) }
        return replacedEntry
    }

    override fun addConfiguration(configName: String, configuration: C) {
        require(configName != DEFAULT_CONFIG) { "You cannot use 'default' as a configuration name as it is reserved for default configuration" }
        configurations[configName] = configuration
    }

    override fun getConfiguration(configName: String): C? = configurations[configName]

    override fun getDefaultConfig(): C = configurations[DEFAULT_CONFIG]!!

    override fun getTags(): Map<String, String> = registryTags

    override fun getEventPublisher(): Registry.EventPublisher<E> = eventProcessor

    protected fun getAllTags(tags: Map<String, String>): Map<String, String> {
        val allTags = HashMap(Objects.requireNonNull(registryTags, TAGS_MUST_NOT_BE_NULL))
        allTags.putAll(tags)
        return allTags
    }

    private inner class RegistryEventProcessor(
        registryEventConsumers: List<RegistryEventConsumer<E>> = emptyList()
    ) : EventProcessor<RegistryEvent>(), EventConsumer<RegistryEvent>, Registry.EventPublisher<E> {

        init {
            registryEventConsumers.forEach { consumer ->
                onEntryAdded(consumer::onEntryAddedEvent)
                onEntryRemoved(consumer::onEntryRemovedEvent)
                onEntryReplaced(consumer::onEntryReplacedEvent)
            }
        }

        override fun onEntryAdded(onSuccessEventConsumer: EventConsumer<EntryAddedEvent<E>>): Registry.EventPublisher<E> {
            registerConsumer(EntryAddedEvent::class.java.name, onSuccessEventConsumer)
            return this
        }

        override fun onEntryRemoved(onErrorEventConsumer: EventConsumer<EntryRemovedEvent<E>>): Registry.EventPublisher<E> {
            registerConsumer(EntryRemovedEvent::class.java.name, onErrorEventConsumer)
            return this
        }

        override fun onEntryReplaced(onStateTransitionEventConsumer: EventConsumer<EntryReplacedEvent<E>>): Registry.EventPublisher<E> {
            registerConsumer(EntryReplacedEvent::class.java.name, onStateTransitionEventConsumer)
            return this
        }

        override fun consumeEvent(event: RegistryEvent) {
            super.processEvent(event)
        }
    }

    companion object {
        protected const val DEFAULT_CONFIG = "default"
        protected const val CONFIG_MUST_NOT_BE_NULL = "Config must not be null"
        protected const val CONSUMER_MUST_NOT_BE_NULL = "EventConsumers must not be null"
        protected const val SUPPLIER_MUST_NOT_BE_NULL = "Supplier must not be null"
        protected const val TAGS_MUST_NOT_BE_NULL = "Tags must not be null"
        private const val NAME_MUST_NOT_BE_NULL = "Name must not be null"
        private const val REGISTRY_STORE_MUST_NOT_BE_NULL = "Registry Store must not be null"
    }
}
