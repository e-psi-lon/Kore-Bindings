package io.github.e_psi_lon.kore.bindings.generation

import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FunctionParser2Test {

    private val testLogger = Logger.println(level = LogLevel.ERROR)

    @Test
    fun `should extract scoreboards from function content`() {
        // Arrange
        val content = """
            scoreboard objectives add my_score dummy
            scoreboard objectives add another.score dummy "Display Name"
            scoreboard objectives remove old_score
        """.trimIndent()
        val parser = FunctionParser2(content, "test", Path("test.mcfunction"), testLogger)

        // Act
        val (scoreboards, _, _) = parser()

        // Assert
        assertEquals(3, scoreboards.size)
        assertTrue(scoreboards.any { it.name == "my_score" })
        assertTrue(scoreboards.any { it.name == "another.score" })
        assertTrue(scoreboards.any { it.name == "old_score" })
    }

    @Test
    fun `should extract storages from function content`() {
        // Arrange
        val content = """
            data merge storage mypack:data {value: 1}
            data get storage minecraft:global
            data modify storage other:temp set value 42
            data remove storage test:cleanup
        """.trimIndent()
        val parser = FunctionParser2(content, "mypack", Path("test.mcfunction"), testLogger)

        // Act
        val (_, storages, _) = parser()

        // Assert
        assertEquals(4, storages.size)
        assertTrue(storages.any { it.minecraftId == "mypack:data" })
        assertTrue(storages.any { it.minecraftId == "minecraft:global" })
        assertTrue(storages.any { it.minecraftId == "other:temp" })
        assertTrue(storages.any { it.minecraftId == "test:cleanup" })
    }

    @Test
    fun `should extract macro parameters`() {
        // Arrange
        val content = $$"""
            $say Hello $(name)!
            $scoreboard players set @s score $(value)
            $execute positioned $(x) $(y) $(z) run function test:other
        """.trimIndent()
        val parser = FunctionParser2(content, "test", Path("macro_func.mcfunction"), testLogger)

        // Act
        val (_, _, macro) = parser()

        // Assert
        assertNotNull(macro)
        assertEquals(5, macro.parameters.size)
        assertTrue(macro.parameters.contains("name"))
        assertTrue(macro.parameters.contains("value"))
        assertTrue(macro.parameters.contains("x"))
        assertTrue(macro.parameters.contains("y"))
        assertTrue(macro.parameters.contains("z"))
    }

    @Test
    fun `should handle functions without macros`() {
        // Arrange
        val content = """
            say Hello World
            scoreboard objectives add test dummy
        """.trimIndent()
        val parser = FunctionParser2(content, "test", Path("normal.mcfunction"), testLogger)

        // Act
        val (_, _, macro) = parser()

        // Assert
        assertNull(macro)
    }

    @Test
    fun `should strip comments from function content`() {
        // Arrange
        val content = """
            # This is a comment
            scoreboard objectives add real_score dummy
            # scoreboard objectives add commented_score dummy
            say Hello # inline comment
        """.trimIndent()
        val parser = FunctionParser2(content, "test", Path("test.mcfunction"), testLogger)

        // Act
        val (scoreboards, _, _) = parser()

        // Assert
        assertEquals(1, scoreboards.size)
        assertTrue(scoreboards.any { it.name == "real_score" })
    }

    @Test
    fun `should handle multiline commands with backslash`() {
        // Arrange
        val content = """
            scoreboard objectives add \
            my_score dummy
            data merge storage test:data \
            {value: 1}
        """.trimIndent()
        val parser = FunctionParser2(content, "test", Path("test.mcfunction"), testLogger)

        // Act
        val (scoreboards, storages, _) = parser()

        // Assert
        assertEquals(1, scoreboards.size)
        assertTrue(scoreboards.any { it.name == "my_score" })
        assertEquals(1, storages.size)
        assertTrue(storages.any { it.minecraftId == "test:data" })
    }

    @Test
    fun `should handle qualified scoreboard names with namespace inference`() {
        // Arrange
        val content = """
            scoreboard objectives add simple dummy
            scoreboard objectives add bs.ctx dummy
            scoreboard objectives add bs.math.result dummy
        """.trimIndent()
        val parser = FunctionParser2(content, "mypack", Path("test.mcfunction"), testLogger)

        // Act
        val (scoreboards, _, _) = parser()

        // Assert
        assertEquals(3, scoreboards.size)

        // Unqualified scoreboard should belong to current namespace
        val simple = scoreboards.find { it.name == "simple" }
        assertNotNull(simple)
        assertEquals("mypack", simple.sourceNamespace)

        // Qualified scoreboard namespace is everything before last part
        val bsCtx = scoreboards.find { it.name == "bs.ctx" }
        assertNotNull(bsCtx)
        assertEquals("bs", bsCtx.sourceNamespace)

        val bsMathResult = scoreboards.find { it.name == "bs.math.result" }
        assertNotNull(bsMathResult)
        assertEquals("bs.math", bsMathResult.sourceNamespace)
    }

    @Test
    fun `should handle storage with path separators`() {
        // Arrange
        val content = """
            data merge storage test:path/to/data {value: 1}
            data get storage pack:config/settings/main
        """.trimIndent()
        val parser = FunctionParser2(content, "test", Path("test.mcfunction"), testLogger)

        // Act
        val (_, storages, _) = parser()

        // Assert
        assertEquals(2, storages.size)
        assertTrue(storages.any { it.minecraftId == "test:path/to/data" })
        assertTrue(storages.any { it.minecraftId == "pack:config/settings/main" })
    }

    @Test
    fun `should deduplicate macro parameters`() {
        // Arrange
        val content = $$"""
            $say $(name) $(name) $(name)
            $execute as $(name) run say $(value)
        """.trimIndent()
        val parser = FunctionParser2(content, "test", Path("test.mcfunction"), testLogger)

        // Act
        val (_, _, macro) = parser()

        // Assert
        assertNotNull(macro)
        assertEquals(2, macro.parameters.size)
        assertTrue(macro.parameters.contains("name"))
        assertTrue(macro.parameters.contains("value"))
    }

    @Test
    fun `should ignore comments in strings when stripping`() {
        // Arrange
        val content = """
            say "This has a # but it's in a string"
            scoreboard objectives add test1 dummy
            # This is a real comment with scoreboard objectives add fake dummy
            scoreboard objectives add test2 dummy
        """.trimIndent()
        val parser = FunctionParser2(content, "test", Path("test.mcfunction"), testLogger)

        // Act
        val (scoreboards, _, _) = parser()

        // Assert
        assertEquals(2, scoreboards.size)
        assertTrue(scoreboards.any { it.name == "test1" })
        assertTrue(scoreboards.any { it.name == "test2" })
    }

    @Test
    fun `should handle empty function content`() {
        // Arrange
        val content = ""
        val parser = FunctionParser2(content, "test", Path("empty.mcfunction"), testLogger)

        // Act
        val (scoreboards, storages, macro) = parser()

        // Assert
        assertTrue(scoreboards.isEmpty())
        assertTrue(storages.isEmpty())
        assertNull(macro)
    }

    @Test
    fun `should handle function with only comments`() {
        // Arrange
        val content = """
            # Only comments here
            # Nothing to parse
            # Just documentation
        """.trimIndent()
        val parser = FunctionParser2(content, "test", Path("comments.mcfunction"), testLogger)

        // Act
        val (scoreboards, storages, macro) = parser()

        // Assert
        assertTrue(scoreboards.isEmpty())
        assertTrue(storages.isEmpty())
        assertNull(macro)
    }

}
