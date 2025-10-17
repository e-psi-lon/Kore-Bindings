package io.github.e_psi_lon.kore.bindings.generation

import io.github.e_psi_lon.kore.bindings.generation.data.Component
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DatapackParserTest {

    private val testLogger = Logger(useGradleLogger = false, level = Level.ERROR)

    @Test
    fun `should parse simple datapack with single namespace`(@TempDir tempDir: Path) = runTest {
        // Arrange
        val datapackDir = tempDir.resolve("test_datapack")
        val namespace = "mypack"

        createDatapackStructure(datapackDir, namespace) {
            function("test_function", """
                scoreboard objectives add my_score dummy
                data merge storage mypack:data {value: 1}
            """.trimIndent())
        }

        // Act
        val parser = DatapackParser(datapackDir, testLogger)
        val result = parser()

        // Assert
        assertEquals(1, result.namespaces.size)
        val parsedNamespace = result.namespaces.first()
        assertEquals(namespace, parsedNamespace.name)
        assertEquals(1, parsedNamespace.components.filterIsInstance<Component.Function>().size)
        assertTrue(parsedNamespace.localScoreboards.any { it.name == "my_score" })
        assertTrue(parsedNamespace.localStorages.any { it.minecraftId == "mypack:data" })
    }

    @Test
    fun `should parse multiple namespaces in parallel`(@TempDir tempDir: Path) = runTest {
        // Arrange
        val datapackDir = tempDir.resolve("test_datapack")

        createDatapackStructure(datapackDir, "namespace1") {
            function("func1", "scoreboard objectives add score1 dummy")
        }
        createDatapackStructure(datapackDir, "namespace2") {
            function("func2", "scoreboard objectives add score2 dummy")
        }

        // Act
        val parser = DatapackParser(datapackDir, testLogger)
        val result = parser()

        // Assert
        assertEquals(2, result.namespaces.size)
        assertTrue(result.namespaces.any { it.name == "namespace1" })
        assertTrue(result.namespaces.any { it.name == "namespace2" })
    }

    @Test
    fun `should parse functions with macros`(@TempDir tempDir: Path) = runTest {
        // Arrange
        val datapackDir = tempDir.resolve("test_datapack")
        val namespace = "macro_test"

        createDatapackStructure(datapackDir, namespace) {
            function("macro_func", $$"""
                $say Hello $(name)!
                $scoreboard players set @s my_score $(value)
            """.trimIndent())
        }

        // Act
        val parser = DatapackParser(datapackDir, testLogger)
        val result = parser()

        // Assert
        val parsedNamespace = result.namespaces.first()
        assertEquals(1, parsedNamespace.macros.size)
        val macro = parsedNamespace.macros.first()
        assertEquals("macro_func", macro.functionName)
        assertTrue(macro.parameters.contains("name"))
        assertTrue(macro.parameters.contains("value"))
    }

    @Test
    fun `should handle prefixed namespaces`(@TempDir tempDir: Path) = runTest {
        // Arrange
        val datapackDir = tempDir.resolve("test_datapack")
        val namespace = "bs.math"

        createDatapackStructure(datapackDir, namespace) {
            function("calculate", "scoreboard objectives add bs.math.result dummy")
        }

        // Act
        val parser = DatapackParser(datapackDir, testLogger)
        val result = parser()

        // Assert
        assertEquals(1, result.namespaces.size)
        val parsedNamespace = result.namespaces.first()
        assertEquals("bs.math", parsedNamespace.name)
        assertEquals("bs", parsedNamespace.prefix)
        assertTrue(parsedNamespace.isGrouped)
    }

    @Test
    fun `should parse different component types`(@TempDir tempDir: Path) = runTest {
        // Arrange
        val datapackDir = tempDir.resolve("test_datapack")
        val namespace = "multi_component"

        createDatapackStructure(datapackDir, namespace) {
            function("my_function", "say hello")
            advancement("my_advancement", """{"criteria": {"impossible": {"trigger": "impossible"}}}""")
            recipe("my_recipe", """{"type": "crafting_shaped", "pattern": ["###"], "key": {"#": {"item": "minecraft:stick"}}, "result": {"item": "minecraft:arrow"}}""")
        }

        // Act
        val parser = DatapackParser(datapackDir, testLogger)
        val result = parser()

        // Assert
        val parsedNamespace = result.namespaces.first()
        assertTrue(parsedNamespace.components.any { it is Component.Function && it.fileName == "my_function" })
        assertTrue(parsedNamespace.components.any { it is Component.Simple && it.fileName == "my_advancement" })
        assertTrue(parsedNamespace.components.any { it is Component.Simple && it.fileName == "my_recipe" })
    }

    @Test
    fun `should parse nested function directories`(@TempDir tempDir: Path) = runTest {
        // Arrange
        val datapackDir = tempDir.resolve("test_datapack")
        val namespace = "nested"

        createDatapackStructure(datapackDir, namespace) {
            function("top_level", "say top")
            function("subdir/nested_func", "say nested")
            function("subdir/deep/very_nested", "say very nested")
        }

        // Act
        val parser = DatapackParser(datapackDir, testLogger)
        val result = parser()

        // Assert
        val parsedNamespace = result.namespaces.first()
        val functions = parsedNamespace.components.filterIsInstance<Component.Function>()
        assertEquals(3, functions.size)

        val nestedFunc = functions.find { it.fileName == "nested_func" }
        assertNotNull(nestedFunc)
        assertEquals(listOf("subdir"), nestedFunc.directoryHierarchy)

        val veryNested = functions.find { it.fileName == "very_nested" }
        assertNotNull(veryNested)
        assertEquals(listOf("subdir", "deep"), veryNested.directoryHierarchy)
    }

    @Test
    fun `should extract storages with correct namespace parsing`(@TempDir tempDir: Path) = runTest {
        // Arrange
        val datapackDir = tempDir.resolve("test_datapack")
        val namespace = "storage_test"

        createDatapackStructure(datapackDir, namespace) {
            function("use_storage", """
                data merge storage storage_test:local {value: 1}
                data get storage minecraft:global
                data modify storage other:external set value 42
            """.trimIndent())
        }

        // Act
        val parser = DatapackParser(datapackDir, testLogger)
        val result = parser()

        // Assert
        val parsedNamespace = result.namespaces.first()
        assertEquals(3, parsedNamespace.localStorages.size)

        assertTrue(parsedNamespace.localStorages.any {
            it.namespace == "storage_test" && it.name == "local"
        })
        assertTrue(parsedNamespace.localStorages.any {
            it.namespace == "minecraft" && it.name == "global"
        })
        assertTrue(parsedNamespace.localStorages.any {
            it.namespace == "other" && it.name == "external"
        })
    }

    @Test
    fun `should handle empty datapack`(@TempDir tempDir: Path) = runTest {
        // Arrange
        val datapackDir = tempDir.resolve("empty_datapack")
        datapackDir.resolve("data").createDirectories()

        // Act
        val parser = DatapackParser(datapackDir, testLogger)
        val result = parser()

        // Assert
        assertTrue(result.namespaces.isEmpty())
        assertTrue(result.namespaceGroups.isEmpty())
    }

    @Test
    fun `should parse function tags`(@TempDir tempDir: Path) = runTest {
        // Arrange
        val datapackDir = tempDir.resolve("test_datapack")
        val namespace = "tags_test"

        createDatapackStructure(datapackDir, namespace) {
            functionTag("load", """{"values": ["tags_test:init"]}""")
            functionTag("tick", """{"values": ["tags_test:update"]}""")
        }

        // Act
        val parser = DatapackParser(datapackDir, testLogger)
        val result = parser()

        // Assert
        val parsedNamespace = result.namespaces.first()
        val functionTags = parsedNamespace.components.filterIsInstance<Component.FunctionTag>()
        assertEquals(2, functionTags.size)
        assertTrue(functionTags.any { it.fileName == "load" })
        assertTrue(functionTags.any { it.fileName == "tick" })
    }

    // Helper DSL for creating test datapack structures
    private fun createDatapackStructure(
        datapackDir: Path,
        namespace: String,
        builder: DatapackBuilder.() -> Unit
    ) {
        val dataDir = datapackDir.resolve("data").resolve(namespace)
        DatapackBuilder(dataDir).apply(builder)
    }

    private class DatapackBuilder(private val namespaceDir: Path) {
        fun function(path: String, content: String) {
            val functionFile = namespaceDir.resolve("function").resolve("$path.mcfunction")
            functionFile.parent.createDirectories()
            functionFile.writeText(content)
        }

        fun advancement(path: String, content: String) {
            val file = namespaceDir.resolve("advancement").resolve("$path.json")
            file.parent.createDirectories()
            file.writeText(content)
        }

        fun recipe(path: String, content: String) {
            val file = namespaceDir.resolve("recipe").resolve("$path.json")
            file.parent.createDirectories()
            file.writeText(content)
        }

        fun functionTag(path: String, content: String) {
            val file = namespaceDir.resolve("tags/function").resolve("$path.json")
            file.parent.createDirectories()
            file.writeText(content)
        }
    }
}
