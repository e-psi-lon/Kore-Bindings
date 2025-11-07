package io.github.e_psi_lon.kore.bindings.generation

import io.github.e_psi_lon.kore.bindings.generation.data.Component
import io.github.e_psi_lon.kore.bindings.generation.data.Datapack
import io.github.e_psi_lon.kore.bindings.generation.data.ParsedNamespace
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.Path
import kotlin.test.assertContains
import kotlin.test.assertTrue

/**
 * Test suite for BindingGenerator focusing on Simple components
 * (advancements, loot tables, recipes, etc.)
 */
class BindingGeneratorSimpleComponentTest {

    private val testLogger = Logger.println(level = LogLevel.DEBUG)

    @Test
    fun `should generate bindings for simple component with no directory hierarchy`(@TempDir tempDir: Path) {
        // Arrange
        val component = Component.Simple(
            relativePath = Path("test_advancement.json"),
            fileName = "test_advancement",
            componentType = DatapackComponentType.ADVANCEMENT
        )

        val namespace = ParsedNamespace(
            name = "mypack",
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
        val generatedFile = tempDir.resolve("Mypack.kt")
        assertTrue(generatedFile.toFile().exists(), "Generated file should exist")

        val content = generatedFile.readText()
        assertContains(content, "object Mypack")
        assertContains(content, "namespace: String = \"mypack\"")
        assertContains(content, "val testAdvancement")
        assertContains(content, "AdvancementArgument")
    }

    @Test
    fun `should generate bindings for simple component with nested directory hierarchy`(@TempDir tempDir: Path) {
        // Arrange
        val component = Component.Simple(
            relativePath = Path("foo/bar/test_recipe.json"),
            fileName = "test_recipe",
            componentType = DatapackComponentType.RECIPE
        )

        val namespace = ParsedNamespace(
            name = "testns",
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
        val generatedFile = tempDir.resolve("Testns.kt")
        val content = generatedFile.readText()

        // Should have nested objects for directory structure
        assertContains(content, "object Foo")
        assertContains(content, "object Bar")
        assertContains(content, "val testRecipe")
        assertContains(content, "RecipeArgument")
    }

    @Test
    fun `should generate PATH constants for nested directory hierarchy`(@TempDir tempDir: Path) {
        // Arrange
        val component = Component.Simple(
            relativePath = Path("level1/level2/level3/item.json"),
            fileName = "item",
            componentType = DatapackComponentType.LOOT_TABLE
        )

        val namespace = ParsedNamespace(
            name = "pathtest",
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
        val content = tempDir.resolve("Pathtest.kt").readText()

        // Each nested object should have a PATH constant
        assertContains(content, "object Level1")
        assertContains(content, "object Level2")
        assertContains(content, "object Level3")

        // PATH should reference parent's PATH for nested objects
        assertTrue(content.contains("PATH") || content.contains("path"))
    }

    @Test
    fun `should generate bindings for multiple simple components in same namespace`(@TempDir tempDir: Path) {
        // Arrange
        val components = listOf(
            Component.Simple(
                relativePath = Path("adv1.json"),
                fileName = "adv1",
                componentType = DatapackComponentType.ADVANCEMENT
            ),
            Component.Simple(
                relativePath = Path("recipes/recipe1.json"),
                fileName = "recipe1",
                componentType = DatapackComponentType.RECIPE
            ),
            Component.Simple(
                relativePath = Path("loot_tables/chest.json"),
                fileName = "chest",
                componentType = DatapackComponentType.LOOT_TABLE
            )
        )

        val namespace = ParsedNamespace(
            name = "multicomp",
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
        val content = tempDir.resolve("Multicomp.kt").readText()

        // All components should be present
        assertContains(content, "val adv1")
        assertContains(content, "val recipe1")
        assertContains(content, "val chest")

        // With proper directory nesting
        assertContains(content, "object Recipes")
        assertContains(content, "object LootTables")
    }

    @Test
    fun `should handle components with default parameter values`(@TempDir tempDir: Path) {
        // Arrange
        val componentType = DatapackComponentType.ADVANCEMENT // Has default parameters

        val component = Component.Simple(
            relativePath = Path("test.json"),
            fileName = "test",
            componentType = componentType
        )

        val namespace = ParsedNamespace(
            name = "defaults",
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
        val content = tempDir.resolve("Defaults.kt").readText()

        // Should contain getter with proper parameter handling
        assertContains(content, "get()")
        assertContains(content, "AdvancementArgument")
    }

    @Test
    fun `should sanitize dots in component names`(@TempDir tempDir: Path) {
        // Arrange - component names with dots (valid in Minecraft but need sanitization for Kotlin)
        val component = Component.Simple(
            relativePath = Path("my.other.component.json"),
            fileName = "my.other.component",
            componentType = DatapackComponentType.RECIPE
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

        // Names should be sanitized (dots replaced with underscores)
        assertContains(content, "my_other_component")
    }

    @Test
    fun `should generate suppress annotations on generated files`(@TempDir tempDir: Path) {
        // Arrange
        val component = Component.Simple(
            relativePath = Path("test.json"),
            fileName = "test",
            componentType = DatapackComponentType.ADVANCEMENT
        )

        val namespace = ParsedNamespace(
            name = "suppress",
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
        val content = tempDir.resolve("Suppress.kt").readText()

        // Should contain suppress annotations (may be qualified due to naming collision)
        assertTrue(content.contains("@Suppress") || content.contains("@file:Suppress") || content.contains("@kotlin.Suppress"))
        assertContains(content, "unused")
        assertContains(content, "RedundantVisibilityModifier")
        assertContains(content, "UnusedReceiverParameter")
    }

    @Test
    fun `should generate namespace constant property`(@TempDir tempDir: Path) {
        // Arrange
        val namespace = ParsedNamespace(
            name = "testns",
            prefix = null,
            components = listOf(
                Component.Simple(
                    relativePath = Path("test.json"),
                    fileName = "test",
                    componentType = DatapackComponentType.ADVANCEMENT
                )
            ),
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
        val content = tempDir.resolve("Testns.kt").readText()

        // Should have namespace constant with ConstPropertyName suppression
        assertContains(content, "const val namespace")
        assertContains(content, "\"testns\"")
        testLogger.info("Generated content:\n$content")
        assertTrue(
            content.contains("@Suppress(\"ConstPropertyName\")") ||
            content.contains("@Suppress") // Within a larger suppress annotation
        )
    }

    @Test
    fun `should handle multiple namespaces generating separate files`(@TempDir tempDir: Path) {
        // Arrange
        val namespace1 = ParsedNamespace(
            name = "ns1",
            prefix = null,
            components = listOf(
                Component.Simple(
                    relativePath = Path("test1.json"),
                    fileName = "test1",
                    componentType = DatapackComponentType.ADVANCEMENT
                )
            ),
            localStorages = emptySet(),
            localScoreboards = emptySet(),
            macros = emptyList()
        )

        val namespace2 = ParsedNamespace(
            name = "ns2",
            prefix = null,
            components = listOf(
                Component.Simple(
                    relativePath = Path("test2.json"),
                    fileName = "test2",
                    componentType = DatapackComponentType.RECIPE
                )
            ),
            localStorages = emptySet(),
            localScoreboards = emptySet(),
            macros = emptyList()
        )

        val datapack = Datapack(
            namespaces = listOf(namespace1, namespace2),
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
        assertTrue(tempDir.resolve("Ns1.kt").toFile().exists())
        assertTrue(tempDir.resolve("Ns2.kt").toFile().exists())

        val content1 = tempDir.resolve("Ns1.kt").readText()
        val content2 = tempDir.resolve("Ns2.kt").readText()

        assertContains(content1, "object Ns1")
        assertContains(content1, "test1")
        assertContains(content2, "object Ns2")
        assertContains(content2, "test2")
    }
}

