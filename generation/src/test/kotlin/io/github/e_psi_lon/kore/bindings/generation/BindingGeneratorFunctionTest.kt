package io.github.e_psi_lon.kore.bindings.generation

import io.github.e_psi_lon.kore.bindings.generation.data.Component
import io.github.e_psi_lon.kore.bindings.generation.data.Datapack
import io.github.e_psi_lon.kore.bindings.generation.data.Macro
import io.github.e_psi_lon.kore.bindings.generation.data.ParsedNamespace
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.test.assertContains
import kotlin.test.assertTrue

/**
 * Test suite for BindingGenerator focusing on Function components
 * and their various overloads (simple functions, macros, etc.)
 */
class BindingGeneratorFunctionTest {

    private val testLogger = Logger.println(level = LogLevel.ERROR)

    @Test
    fun `should generate basic function bindings without macro`(@TempDir tempDir: Path) {
        // Arrange
        val component = Component.Function(
            relativePath = Path("test_function.mcfunction"),
            fileName = "test_function",
            macro = null,
            componentType = DatapackComponentType.FUNCTION
        )

        val namespace = ParsedNamespace(
            name = "functest",
            prefix = null,
            components = listOf(component),
            localStorages = emptySet(),
            localScoreboards = emptySet(),
            macros = emptyList()
        )

        val datapack = Datapack(
            namespaces = listOf(namespace),
            namespaceGroups = emptyList()
        )

        val generator = BindingGenerator(
            logger = testLogger,
            datapack = datapack,
            outputDir = tempDir,
            packageName = "io.test.bindings",
            parentPackage = "io.test",
            prefix = null
        )

        // Act
        generator()

        // Assert
        val content = tempDir.resolve("Functest.kt").readText()

        // Should generate FunctionArgument property
        assertContains(content, "val testFunction")
        assertContains(content, "FunctionArgument")

        // Should generate context receiver function
        assertContains(content, "fun testFunction")
        assertContains(content, "context(")
        assertContains(content, "Function")
    }

    @Test
    fun `should generate function bindings with nested directory structure`(@TempDir tempDir: Path) {
        // Arrange
        val component = Component.Function(
            relativePath = Path("util/helper/calc.mcfunction"),
            fileName = "calc",
            macro = null,
            componentType = DatapackComponentType.FUNCTION
        )

        val namespace = ParsedNamespace(
            name = "nested",
            prefix = null,
            components = listOf(component),
            localStorages = emptySet(),
            localScoreboards = emptySet(),
            macros = emptyList()
        )

        val datapack = Datapack(
            namespaces = listOf(namespace),
            namespaceGroups = emptyList()
        )

        val generator = BindingGenerator(
            logger = testLogger,
            datapack = datapack,
            outputDir = tempDir,
            packageName = "io.test.bindings",
            parentPackage = "io.test",
            prefix = null
        )

        // Act
        generator()

        // Assert
        val content = tempDir.resolve("Nested.kt").readText()

        // Should have nested objects
        assertContains(content, "object Util")
        assertContains(content, "object Helper")

        // Function should be in the innermost object
        assertContains(content, "val calc")
        assertContains(content, "fun calc")
    }

    @Test
    fun `should generate macro function with named parameter overload`(@TempDir tempDir: Path) {
        // Arrange
        val macro = Macro(
            parameters = listOf("name", "message")
        )

        val component = Component.Function(
            relativePath = Path("greet.mcfunction"),
            fileName = "greet",
            macro = macro,
            componentType = DatapackComponentType.FUNCTION
        )

        val namespace = ParsedNamespace(
            name = "macrotest",
            prefix = null,
            components = listOf(component),
            localStorages = emptySet(),
            localScoreboards = emptySet(),
            macros = listOf(macro)
        )

        val datapack = Datapack(
            namespaces = listOf(namespace),
            namespaceGroups = emptyList()
        )

        val generator = BindingGenerator(
            logger = testLogger,
            datapack = datapack,
            outputDir = tempDir,
            packageName = "io.test.bindings",
            parentPackage = "io.test",
            prefix = null
        )

        // Act
        generator()

        // Assert
        val content = tempDir.resolve("Macrotest.kt").readText()

        // Should have basic function property
        assertContains(content, "val greet")

        // Should have multiple overloads for macro
        // 2. Named parameters overload
        assertTrue(content.contains("name: String") && content.contains("message: String"))

        // 3. NBT builder overload (lambda type)
        assertContains(content, "nbt:")
        assertContains(content, "Function1")

        // 4. DataArgument overload
        assertContains(content, "arguments: DataArgument")
    }

    @Test
    fun `should generate macro function with single parameter`(@TempDir tempDir: Path) {
        // Arrange
        val macro = Macro(
            parameters = listOf("x")
        )

        val component = Component.Function(
            relativePath = Path("teleport.mcfunction"),
            fileName = "teleport",
            macro = macro,
            componentType = DatapackComponentType.FUNCTION
        )

        val namespace = ParsedNamespace(
            name = "singleparam",
            prefix = null,
            components = listOf(component),
            localStorages = emptySet(),
            localScoreboards = emptySet(),
            macros = listOf(macro)
        )

        val datapack = Datapack(
            namespaces = listOf(namespace),
            namespaceGroups = emptyList()
        )

        val generator = BindingGenerator(
            logger = testLogger,
            datapack = datapack,
            outputDir = tempDir,
            packageName = "io.test.bindings",
            parentPackage = "io.test",
            prefix = null
        )

        // Act
        generator()

        // Assert
        val content = tempDir.resolve("Singleparam.kt").readText()

        // Should have parameter in one of the overloads
        assertContains(content, "x: String")
    }

    @Test
    fun `should generate macro function with many parameters`(@TempDir tempDir: Path) {
        // Arrange
        val macro = Macro(
            parameters = listOf("x", "y", "z", "entity", "count")
        )

        val component = Component.Function(
            relativePath = Path("spawn.mcfunction"),
            fileName = "spawn",
            macro = macro,
            componentType = DatapackComponentType.FUNCTION
        )

        val namespace = ParsedNamespace(
            name = "multiparams",
            prefix = null,
            components = listOf(component),
            localStorages = emptySet(),
            localScoreboards = emptySet(),
            macros = listOf(macro)
        )

        val datapack = Datapack(
            namespaces = listOf(namespace),
            namespaceGroups = emptyList()
        )

        val generator = BindingGenerator(
            logger = testLogger,
            datapack = datapack,
            outputDir = tempDir,
            packageName = "io.test.bindings",
            parentPackage = "io.test",
            prefix = null
        )

        // Act
        generator()

        // Assert
        val content = tempDir.resolve("Multiparams.kt").readText()

        // All parameters should be present in named parameter overload
        assertContains(content, "x: String")
        assertContains(content, "y: String")
        assertContains(content, "z: String")
        assertContains(content, "entity: String")
        assertContains(content, "count: String")
    }

    @Test
    fun `should sanitize function names starting with numbers`(@TempDir tempDir: Path) {
        // Arrange - function name starts with number (invalid Kotlin identifier)
        val component = Component.Function(
            relativePath = Path("123_invalid.mcfunction"),
            fileName = "123_invalid",
            macro = null,
            componentType = DatapackComponentType.FUNCTION
        )

        val namespace = ParsedNamespace(
            name = "invalidname",
            prefix = null,
            components = listOf(component),
            localStorages = emptySet(),
            localScoreboards = emptySet(),
            macros = emptyList()
        )

        val datapack = Datapack(
            namespaces = listOf(namespace),
            namespaceGroups = emptyList()
        )

        val generator = BindingGenerator(
            logger = testLogger,
            datapack = datapack,
            outputDir = tempDir,
            packageName = "io.test.bindings",
            parentPackage = "io.test",
            prefix = null
        )

        // Act
        generator()

        // Assert
        val content = tempDir.resolve("Invalidname.kt").readText()

        // Should prefix with 'n' to make it a valid identifier
        assertContains(content, "n123Invalid")
    }

    @Test
    fun `should generate multiple function bindings in same namespace`(@TempDir tempDir: Path) {
        // Arrange
        val components = listOf(
            Component.Function(
                relativePath = Path("init.mcfunction"),
                fileName = "init",
                macro = null,
                componentType = DatapackComponentType.FUNCTION
            ),
            Component.Function(
                relativePath = Path("tick.mcfunction"),
                fileName = "tick",
                macro = null,
                componentType = DatapackComponentType.FUNCTION
            ),
            Component.Function(
                relativePath = Path("util/helper.mcfunction"),
                fileName = "helper",
                macro = Macro(listOf("value")),
                componentType = DatapackComponentType.FUNCTION
            )
        )

        val namespace = ParsedNamespace(
            name = "multifunc",
            prefix = null,
            components = components,
            localStorages = emptySet(),
            localScoreboards = emptySet(),
            macros = components.mapNotNull { it.macro }
        )

        val datapack = Datapack(
            namespaces = listOf(namespace),
            namespaceGroups = emptyList()
        )

        val generator = BindingGenerator(
            logger = testLogger,
            datapack = datapack,
            outputDir = tempDir,
            packageName = "io.test.bindings",
            parentPackage = "io.test",
            prefix = null
        )

        // Act
        generator()

        // Assert
        val content = tempDir.resolve("Multifunc.kt").readText()

        // All functions should be present (init is escaped as it's a keyword)
        assertTrue(content.contains("val `init`"))
        assertContains(content, "val tick")
        assertContains(content, "val helper")

        // Macro should have overloads (value is escaped as it's a keyword)
        assertTrue(content.contains("`value`: String"))
    }

    @Test
    fun `should use context receiver for all function variants`(@TempDir tempDir: Path) {
        // Arrange
        val macro = Macro(
            parameters = listOf("param1")
        )

        val component = Component.Function(
            relativePath = Path("test.mcfunction"),
            fileName = "test",
            macro = macro,
            componentType = DatapackComponentType.FUNCTION
        )

        val namespace = ParsedNamespace(
            name = "context",
            prefix = null,
            components = listOf(component),
            localStorages = emptySet(),
            localScoreboards = emptySet(),
            macros = listOf(macro)
        )

        val datapack = Datapack(
            namespaces = listOf(namespace),
            namespaceGroups = emptyList()
        )

        val generator = BindingGenerator(
            logger = testLogger,
            datapack = datapack,
            outputDir = tempDir,
            packageName = "io.test.bindings",
            parentPackage = "io.test",
            prefix = null
        )

        // Act
        generator()

        // Assert
        val content = tempDir.resolve("Context.kt").readText()

        // All function overloads should have context receiver
        val functionCount = content.split("fun test").size - 1

        // Should have multiple function declarations (basic + named params + nbt + data argument)
        assertTrue(functionCount >= 3, "Should have at least 3 function overloads for macro")

        // All should use context receiver
        assertContains(content, "context(")
    }

    @Test
    fun `should handle function with dots in name`(@TempDir tempDir: Path) {
        // Arrange - dots are valid in Minecraft but need sanitization for Kotlin
        val component = Component.Function(
            relativePath = Path("my.func.test.mcfunction"),
            fileName = "my.func.test",
            macro = null,
            componentType = DatapackComponentType.FUNCTION
        )

        val namespace = ParsedNamespace(
            name = "sanitize",
            prefix = null,
            components = listOf(component),
            localStorages = emptySet(),
            localScoreboards = emptySet(),
            macros = emptyList()
        )

        val datapack = Datapack(
            namespaces = listOf(namespace),
            namespaceGroups = emptyList()
        )

        val generator = BindingGenerator(
            logger = testLogger,
            datapack = datapack,
            outputDir = tempDir,
            packageName = "io.test.bindings",
            parentPackage = "io.test",
            prefix = null
        )

        // Act
        generator()

        // Assert
        val content = tempDir.resolve("Sanitize.kt").readText()

        // Dots should be replaced with underscores
        assertContains(content, "my_func_test")
    }

    @Test
    fun `should reference correct namespace in generated function calls`(@TempDir tempDir: Path) {
        // Arrange
        val component = Component.Function(
            relativePath = Path("custom_func.mcfunction"),
            fileName = "custom_func",
            macro = null,
            componentType = DatapackComponentType.FUNCTION
        )

        val namespace = ParsedNamespace(
            name = "customns",
            prefix = null,
            components = listOf(component),
            localStorages = emptySet(),
            localScoreboards = emptySet(),
            macros = emptyList()
        )

        val datapack = Datapack(
            namespaces = listOf(namespace),
            namespaceGroups = emptyList()
        )

        val generator = BindingGenerator(
            logger = testLogger,
            datapack = datapack,
            outputDir = tempDir,
            packageName = "io.test.bindings",
            parentPackage = "io.test",
            prefix = null
        )

        // Act
        generator()

        // Assert
        val content = tempDir.resolve("Customns.kt").readText()

        // FunctionArgument property should use correct namespace
        assertTrue(content.contains("\"custom_func\"") && content.contains("\"customns\""))
    }

    @Test
    fun `should generate DataArgument overload with optional path parameter`(@TempDir tempDir: Path) {
        // Arrange
        val macro = Macro(
            parameters = listOf("key")
        )

        val component = Component.Function(
            relativePath = Path("data_func.mcfunction"),
            fileName = "data_func",
            macro = macro,
            componentType = DatapackComponentType.FUNCTION
        )

        val namespace = ParsedNamespace(
            name = "datatest",
            prefix = null,
            components = listOf(component),
            localStorages = emptySet(),
            localScoreboards = emptySet(),
            macros = listOf(macro)
        )

        val datapack = Datapack(
            namespaces = listOf(namespace),
            namespaceGroups = emptyList()
        )

        val generator = BindingGenerator(
            logger = testLogger,
            datapack = datapack,
            outputDir = tempDir,
            packageName = "io.test.bindings",
            parentPackage = "io.test",
            prefix = null
        )

        // Act
        generator()

        // Assert
        val content = tempDir.resolve("Datatest.kt").readText()


        // Should have DataArgument overload
        assertContains(content, "arguments: DataArgument")

        // Path parameter should be optional (nullable with default null)
        assertContains(content, "path: String? = null")
    }

    @Test
    fun `should generate NBT builder overload with default empty lambda`(@TempDir tempDir: Path) {
        // Arrange
        val macro = Macro(
            parameters = listOf("tag")
        )

        val component = Component.Function(
            relativePath = Path("nbt_func.mcfunction"),
            fileName = "nbt_func",
            macro = macro,
            componentType = DatapackComponentType.FUNCTION
        )

        val namespace = ParsedNamespace(
            name = "nbttest",
            prefix = null,
            components = listOf(component),
            localStorages = emptySet(),
            localScoreboards = emptySet(),
            macros = listOf(macro)
        )

        val datapack = Datapack(
            namespaces = listOf(namespace),
            namespaceGroups = emptyList()
        )

        val generator = BindingGenerator(
            logger = testLogger,
            datapack = datapack,
            outputDir = tempDir,
            packageName = "io.test.bindings",
            parentPackage = "io.test",
            prefix = null
        )

        // Act
        generator()

        // Assert
        val content = tempDir.resolve("Nbttest.kt").readText()

        // Should have NBT builder parameter (lambda type)
        assertContains(content, "Function1")
        assertContains(content, "nbt:")

        // Should have default value
        assertTrue(content.contains("= {}") || content.contains("defaultValue"))
    }
}
