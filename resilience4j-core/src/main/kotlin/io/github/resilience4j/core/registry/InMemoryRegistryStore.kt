package io.github.resilience4j.core.registry

import io.github.resilience4j.core.RegistryStore
import java.util.concurrent.ConcurrentHashMap

class InMemoryRegistryStore<E> : RegistryStore<E> {
    private val entryMap = ConcurrentHashMap<String, E>()

    override fun computeIfAbsent(key: String, mappingFunction: (String) -> E): E =
        entryMap.computeIfAbsent(key) { mappingFunction(it) }

    override fun putIfAbsent(key: String, value: E): E? = entryMap.putIfAbsent(key, value)

    override fun find(key: String): E? = entryMap[key]

    override fun remove(name: String): E? = entryMap.remove(name)

    override fun replace(name: String, newEntry: E): E? = entryMap.replace(name, newEntry)

    override fun values(): Collection<E> = entryMap.values
}
