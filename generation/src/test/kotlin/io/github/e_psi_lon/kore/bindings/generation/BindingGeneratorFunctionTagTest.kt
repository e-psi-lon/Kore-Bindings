package io.github.e_psi_lon.kore.bindings.generation

import io.github.e_psi_lon.kore.bindings.generation.data.Component
import io.github.e_psi_lon.kore.bindings.generation.data.Datapack
import io.github.e_psi_lon.kore.bindings.generation.data.ParsedNamespace
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.test.assertContains
import kotlin.test.assertTrue

/**
 * Test suite for BindingGenerator focusing on FunctionTag components.
 * Function tags are similar to simple components but they:
 * - Require a Function context receiver
 * - Return Command type
 * - Call Function.function() method to generate commands
 */
class BindingGeneratorFunctionTagTest {
    private val testLogger = Logger.println(level = LogLevel.ERROR)

    @Test
    fun `should generate basic function tag bindings without directory hierarchy`(@TempDir tempDir: Path) {
        // Arrange
        val component = Component.FunctionTag(
            relativePath = Path("my_tag.json"),
            fileName = "my_tag",
            componentType = DatapackComponentType.FUNCTION_TAG
        )

        val namespace = ParsedNamespace(
            name = "tagtest",
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
        val content = tempDir.resolve("Tagtest.kt").readText()

        // Should generate function binding with context receiver
        assertContains(content, "fun myTag")
        assertContains(content, "context(")
        assertContains(content, "Function")

        // Should return Command type
        assertContains(content, "Command")

        // Should call Function.function() with tag reference
        assertContains(content, "function(")
    }

    @Test
    fun `should generate function tag bindings with nested directory structure`(@TempDir tempDir: Path) {
        // Arrange
        val component = Component.FunctionTag(
            relativePath = Path("admin/util/special_tag.json"),
            fileName = "special_tag",
            componentType = DatapackComponentType.FUNCTION_TAG
        )

        val namespace = ParsedNamespace(
            name = "nestedtag",
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
        val content = tempDir.resolve("Nestedtag.kt").readText()

        // Should have nested objects for directory structure
        assertContains(content, "object Admin")
        assertContains(content, "object Util")

        // Function tag should be in the innermost object
        assertContains(content, "fun specialTag")
        assertContains(content, "context(")
        assertContains(content, "Function")
    }

    @Test
    fun `should sanitize function tag names starting with numbers`(@TempDir tempDir: Path) {
        // Arrange - tag name starts with number (invalid Kotlin identifier)
        val component = Component.FunctionTag(
            relativePath = Path("123_invalid_tag.json"),
            fileName = "123_invalid_tag",
            componentType = DatapackComponentType.FUNCTION_TAG
        )

        val namespace = ParsedNamespace(
            name = "invalidtagname",
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
        val content = tempDir.resolve("Invalidtagname.kt").readText()

        // Should prefix with 'n' to make it a valid identifier
        assertContains(content, "n123InvalidTag")
    }

    @Test
    fun `should generate multiple function tags in same namespace`(@TempDir tempDir: Path) {
        // Arrange
        val components = listOf(
            Component.FunctionTag(
                relativePath = Path("load_tag.json"),
                fileName = "load_tag",
                componentType = DatapackComponentType.FUNCTION_TAG
            ),
            Component.FunctionTag(
                relativePath = Path("tick_tag.json"),
                fileName = "tick_tag",
                componentType = DatapackComponentType.FUNCTION_TAG
            ),
            Component.FunctionTag(
                relativePath = Path("util/helper_tag.json"),
                fileName = "helper_tag",
                componentType = DatapackComponentType.FUNCTION_TAG
            )
        )

        val namespace = ParsedNamespace(
            name = "multitag",
            prefix = null,
            components = components,
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
        val content = tempDir.resolve("Multitag.kt").readText()

        // All tags should be present
        assertContains(content, "fun loadTag")
        assertContains(content, "fun tickTag")
        assertContains(content, "fun helperTag")

        // All should have context receivers
        assertTrue(content.split("context(").size >= 4, "Should have at least 3 function tags with context receiver")
    }

    @Test
    fun `should use context parameter for function tag functions`(@TempDir tempDir: Path) {
        // Arrange
        val component = Component.FunctionTag(
            relativePath = Path("test_tag.json"),
            fileName = "test_tag",
            componentType = DatapackComponentType.FUNCTION_TAG
        )

        val namespace = ParsedNamespace(
            name = "contexttest",
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
        val content = tempDir.resolve("Contexttest.kt").readText()

        // Should use context receiver
        assertContains(content, "context(function: Function)")

        // Context receiver parameter should be function/function instance
        assertTrue(content.contains("function.function("))
    }

    @Test
    fun `should handle function tag with dots in name`(@TempDir tempDir: Path) {
        // Arrange - dots are valid in Minecraft but need sanitization for Kotlin
        val component = Component.FunctionTag(
            relativePath = Path("my.special.tag.json"),
            fileName = "my.special.tag",
            componentType = DatapackComponentType.FUNCTION_TAG
        )

        val namespace = ParsedNamespace(
            name = "dotsanitize",
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
        val content = tempDir.resolve("Dotsanitize.kt").readText()

        // Dots should be replaced/sanitized
        assertTrue(content.contains("my_special_tag"))
    }

    @Test
    fun `should return Command type for function tag functions`(@TempDir tempDir: Path) {
        // Arrange
        val component = Component.FunctionTag(
            relativePath = Path("cmd_tag.json"),
            fileName = "cmd_tag",
            componentType = DatapackComponentType.FUNCTION_TAG
        )

        val namespace = ParsedNamespace(
            name = "commandtest",
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
        val content = tempDir.resolve("Commandtest.kt").readText()

        // Should return Command type (not FunctionTagArgument)
        assertContains(content, ": Command")

        // Should NOT just return the argument type
        assertTrue(!content.contains("fun cmdTag(): FunctionTagArgument"))
    }

    @Test
    fun `should properly escape Kotlin keywords in function tag names`(@TempDir tempDir: Path) {
        // Arrange
        val component = Component.FunctionTag(
            relativePath = Path("object.json"),
            fileName = "object",  // 'object' is a Kotlin keyword
            componentType = DatapackComponentType.FUNCTION_TAG
        )

        val namespace = ParsedNamespace(
            name = "keywords",
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
        val content = tempDir.resolve("Keywords.kt").readText()

        // Keyword should be backtick-escaped
        assertContains(content, "`object`")
    }

    @Test
    fun `should generate function tag with complex nested paths`(@TempDir tempDir: Path) {
        // Arrange
        val components = listOf(
            Component.FunctionTag(
                relativePath = Path("a/b/c/d/deep_tag.json"),
                fileName = "deep_tag",
                componentType = DatapackComponentType.FUNCTION_TAG
            )
        )

        val namespace = ParsedNamespace(
            name = "deepnest",
            prefix = null,
            components = components,
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
        val content = tempDir.resolve("Deepnest.kt").readText()

        // Should have all nested objects
        assertContains(content, "object A")
        assertContains(content, "object B")
        assertContains(content, "object C")
        assertContains(content, "object D")

        // Tag function should be in deepest level
        assertContains(content, "fun deepTag")
    }

    @Test
    fun `function tag should call Function member method correctly`(@TempDir tempDir: Path) {
        // Arrange
        val component = Component.FunctionTag(
            relativePath = Path("method_test_tag.json"),
            fileName = "method_test_tag",
            componentType = DatapackComponentType.FUNCTION_TAG
        )

        val namespace = ParsedNamespace(
            name = "methodtest",
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
        val content = tempDir.resolve("Methodtest.kt").readText()

        // Should call the function method (from Function::function)
        assertContains(content, "function(")
    }
}

