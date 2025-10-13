package io.github.e_psi_lon.kore.bindings.generation.data

import io.github.e_psi_lon.kore.bindings.generation.DatapackComponentType
import java.nio.file.Path


/**
 * Represents a component in the datapack (function, advancement, etc.)
 */
sealed class Component {
    abstract val relativePath: Path  // Relative to component directory
    abstract val fileName: String
    abstract val componentType: DatapackComponentType

    /**
     * Full path including subdirectories (e.g., "foo/bar/baz")
     */
    val fullPath: String
        get() = if (relativePath.parent != null) {
            "${relativePath.parent}/$fileName"
        } else {
            fileName
        }

    /**
     * Nested directory structure as list (e.g., ["foo", "bar"] for "foo/bar/baz.json")
     */
    val directoryHierarchy: List<String>
        get() = relativePath.parent?.toString()?.split('/')?.filter { it.isNotEmpty() } ?: emptyList()


    /**
     * A simple component (advancement, loot table, etc.)
     */
    data class Simple(
        override val relativePath: Path,
        override val fileName: String,
        override val componentType: DatapackComponentType
    ) : Component()

    /**
     * A function component (may have an associated macro)
     */
    data class Function(
        override val relativePath: Path,
        override val fileName: String,
        val macro: Macro?,
        override val componentType: DatapackComponentType = DatapackComponentType.FUNCTION,
    ) : Component()

    data class FunctionTag(
        override val relativePath: Path,
        override val fileName: String,
        override val componentType: DatapackComponentType = DatapackComponentType.FUNCTION_TAG
    ) : Component()
}

