package io.github.e_psi_lon.kore.bindings.generation.data


class DatapackBuilder {
    private val namespaces = mutableListOf<ParsedNamespace>()

    fun addNamespace(namespace: ParsedNamespace) {
        namespaces.add(namespace)
    }

    fun build(): Datapack {
        val groups = identifyNamespaceGroups()
        return Datapack(namespaces, groups)
    }

    private fun identifyNamespaceGroups(): List<NamespaceGroup> {
        val grouped = namespaces
            .filter { it.name.contains('.') }
            .groupBy { it.name.substringBefore('.') }

        return grouped.values.map { namespacesInGroup ->
            val prefix = namespacesInGroup.first().name.substringBefore('.')
            val sharedResources = findSharedResources(namespacesInGroup)
            NamespaceGroup(
                prefix = prefix,
                subNamespaces = namespacesInGroup.map { it.name },
                sharedResources = sharedResources
            )
        }
    }

    private fun findSharedResources(namespaces: List<ParsedNamespace>): SharedResources {
        if (namespaces.size <= 1) return SharedResources.EMPTY

        // Find storages that appear in multiple namespaces
        val sharedStorages = namespaces.filterShared { it.localStorages }

        // Find scoreboards that appear in multiple namespaces
        val sharedScoreboards = namespaces.filterShared { it.localScoreboards }

        return SharedResources(sharedStorages, sharedScoreboards)
    }

    private fun <T : NamedResource> List<ParsedNamespace>.filterShared(resource: (ParsedNamespace) -> Set<T>): Set<T> {
        val allElements = this.flatMap(resource)
        val elementsByName = allElements.groupBy { it.name }
        val sharedElements = elementsByName
            .filter { it.value.size > 1 }
            .map { it.value.first() }
            .toSet()
        return sharedElements
    }
}