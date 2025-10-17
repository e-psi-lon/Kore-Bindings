package io.github.e_psi_lon.kore.bindings.generation

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FunctionRegexTest {

    // ========================================
    // SCOREBOARD REGEX TESTS
    // ========================================

    @Test
    fun `scoreboard regex should match add command with objective name`() {
        val input = "scoreboard objectives add my_score dummy"
        val matches = scoreboardRegex.findAll(input).toList()

        assertEquals(1, matches.size, "Should find exactly 1 match")
        assertEquals("my_score", matches[0].groupValues[1], "Group 1 should capture objective name")
        assertEquals("", matches[0].groupValues[2], "Group 2 should be empty")
    }

    @Test
    fun `scoreboard regex should match remove command with objective name`() {
        val input = "scoreboard objectives remove old_score"
        val matches = scoreboardRegex.findAll(input).toList()

        assertEquals(1, matches.size)
        assertEquals("old_score", matches[0].groupValues[1])
    }

    @Test
    fun `scoreboard regex should match modify command with objective name`() {
        val input = "scoreboard objectives modify test displayname \"Test\""
        val matches = scoreboardRegex.findAll(input).toList()

        assertEquals(1, matches.size)
        assertEquals("test", matches[0].groupValues[1])
    }

    @Test
    fun `scoreboard regex should match setdisplay with slot AND objective`() {
        val input = "scoreboard objectives setdisplay sidebar my_score"
        val matches = scoreboardRegex.findAll(input).toList()

        assertEquals(1, matches.size, "Should find exactly 1 match")
        assertEquals("", matches[0].groupValues[1], "Group 1 should be empty")
        assertEquals("my_score", matches[0].groupValues[2], "Group 2 should capture objective name")
    }

    @Test
    fun `scoreboard regex should match setdisplay with slot only (no objective)`() {
        val input = "scoreboard objectives setdisplay sidebar"
        val matches = scoreboardRegex.findAll(input).toList()

        assertEquals(1, matches.size, "Should find exactly 1 match")
        assertEquals("", matches[0].groupValues[1], "Group 1 should be empty")
        assertEquals("", matches[0].groupValues[2], "Group 2 should be empty (no objective provided)")
    }

    @Test
    fun `scoreboard regex should handle various slot names`() {
        val slots = listOf("sidebar", "list", "belowName", "sidebar.team.red")

        slots.forEach { slot ->
            val input = "scoreboard objectives setdisplay $slot"
            val matches = scoreboardRegex.findAll(input).toList()
            assertEquals(1, matches.size, "Should match setdisplay with slot: $slot")
            assertEquals("", matches[0].groupValues[1])
            assertEquals("", matches[0].groupValues[2])
        }
    }

    @Test
    fun `scoreboard regex should handle qualified objective names`() {
        val input = "scoreboard objectives add bs.math.result dummy"
        val matches = scoreboardRegex.findAll(input).toList()

        assertEquals(1, matches.size)
        assertEquals("bs.math.result", matches[0].groupValues[1])
    }

    @Test
    fun `scoreboard regex should handle objective names with special characters`() {
        val input = "scoreboard objectives add test_score-2024 dummy"
        val matches = scoreboardRegex.findAll(input).toList()

        assertEquals(1, matches.size)
        assertEquals("test_score-2024", matches[0].groupValues[1])
    }

    @Test
    fun `scoreboard regex should only match full commands with proper boundaries`() {
        val input = "say scoreboard objectives add test dummy"
        val matches = scoreboardRegex.findAll(input).toList()

        assertEquals(0, matches.size, "Should not match when scoreboard is not at word boundary")
    }

    @Test
    fun `scoreboard regex should handle multiple commands in multiline content`() {
        val input = """
            scoreboard objectives add score1 dummy
            scoreboard objectives setdisplay sidebar score1
            scoreboard objectives setdisplay list
            scoreboard objectives add score2 dummy
            scoreboard objectives setdisplay belowname score2
        """.trimIndent()

        val matches = scoreboardRegex.findAll(input).toList()

        // We expect 5 matches total
        assertEquals(5, matches.size, "Should find 5 matches")

        // Verify each match
        assertEquals("score1", matches[0].groupValues[1]) // add score1
        assertEquals("score1", matches[1].groupValues[2]) // setdisplay sidebar score1
        assertEquals("", matches[2].groupValues[1])      // setdisplay list (no objective)
        assertEquals("", matches[2].groupValues[2])      // setdisplay list (no objective)
        assertEquals("score2", matches[3].groupValues[1]) // add score2
        assertEquals("score2", matches[4].groupValues[2]) // setdisplay belowname score2
    }

    // ========================================
    // STORAGE REGEX TESTS
    // ========================================

    @Test
    fun `storage regex should match data get storage command`() {
        val input = "data get storage mypack:data"
        val matches = storageRegex.findAll(input).toList()

        assertEquals(1, matches.size)
        assertEquals("mypack:data", matches[0].groupValues[1])
    }

    @Test
    fun `storage regex should match data merge storage command`() {
        val input = "data merge storage test:config {value: 1}"
        val matches = storageRegex.findAll(input).toList()

        assertEquals(1, matches.size)
        assertEquals("test:config", matches[0].groupValues[1])
    }

    @Test
    fun `storage regex should match data modify storage command`() {
        val input = "data modify storage temp:data set value 42"
        val matches = storageRegex.findAll(input).toList()

        assertEquals(1, matches.size)
        assertEquals("temp:data", matches[0].groupValues[1])
    }

    @Test
    fun `storage regex should match data remove storage command`() {
        val input = "data remove storage cleanup:temp path.to.data"
        val matches = storageRegex.findAll(input).toList()

        assertEquals(1, matches.size)
        assertEquals("cleanup:temp", matches[0].groupValues[1])
    }

    @Test
    fun `storage regex should handle storage paths with slashes`() {
        val input = "data get storage mypack:path/to/data"
        val matches = storageRegex.findAll(input).toList()

        assertEquals(1, matches.size)
        assertEquals("mypack:path/to/data", matches[0].groupValues[1])
    }

    @Test
    fun `storage regex should handle storage paths with underscores and dashes`() {
        val input = "data get storage my_pack:my-data.test"
        val matches = storageRegex.findAll(input).toList()

        assertEquals(1, matches.size)
        assertEquals("my_pack:my-data.test", matches[0].groupValues[1])
    }

    @Test
    fun `storage regex should not match data commands on entities or blocks`() {
        val entityInput = "data get entity @s Inventory"
        val blockInput = "data get block ~ ~ ~ Items"

        assertEquals(0, storageRegex.findAll(entityInput).count(), "Should not match entity data")
        assertEquals(0, storageRegex.findAll(blockInput).count(), "Should not match block data")
    }

    // ========================================
    // MACRO LINE REGEX TESTS
    // ========================================

    @Test
    fun `macro line regex should match lines starting with dollar sign`() {
        val input = "\$say Hello World"
        val matches = macroLineRegex.findAll(input).toList()

        assertEquals(1, matches.size)
        assertEquals("say Hello World", matches[0].groupValues[1])
    }

    @Test
    fun `macro line regex should match lines with variables`() {
        val input = "\$say Hello \$(name)!"
        val matches = macroLineRegex.findAll(input).toList()

        assertEquals(1, matches.size)
        assertEquals("say Hello \$(name)!", matches[0].groupValues[1])
    }

    @Test
    fun `macro line regex should not match non-macro lines`() {
        val input = "say This is not a macro"
        val matches = macroLineRegex.findAll(input).toList()

        assertEquals(0, matches.size, "Should not match lines without $ prefix")
    }

    @Test
    fun `macro line regex should handle multiline content with MULTILINE option`() {
        val input = """
            say Normal command
            ${'$'}say Macro command
            data get storage test:data
            ${'$'}teleport @s ~ ~${'$'}(y) ~
        """.trimIndent()

        val matches = macroLineRegex.findAll(input).toList()

        assertEquals(2, matches.size, "Should find 2 macro lines")
        assertEquals("say Macro command", matches[0].groupValues[1])
        assertEquals("teleport @s ~ ~\$(y) ~", matches[1].groupValues[1])
    }

    // ========================================
    // MACRO PARAMETER REGEX TESTS
    // ========================================

    @Test
    fun `macro parameter regex should extract variable names`() {
        val input = "\$(name)"
        val matches = macroParameterRegex.findAll(input).toList()

        assertEquals(1, matches.size)
        assertEquals("name", matches[0].groupValues[1])
    }

    @Test
    fun `macro parameter regex should extract multiple variables`() {
        val input = "teleport @s \$(x) \$(y) \$(z)"
        val matches = macroParameterRegex.findAll(input).toList()

        assertEquals(3, matches.size)
        assertEquals("x", matches[0].groupValues[1])
        assertEquals("y", matches[1].groupValues[1])
        assertEquals("z", matches[2].groupValues[1])
    }

    @Test
    fun `macro parameter regex should handle variables with numbers and underscores`() {
        val input = "\$(key_1) \$(value2) \$(test_var_123)"
        val matches = macroParameterRegex.findAll(input).toList()

        assertEquals(3, matches.size)
        assertEquals("key_1", matches[0].groupValues[1])
        assertEquals("value2", matches[1].groupValues[1])
        assertEquals("test_var_123", matches[2].groupValues[1])
    }

    @Test
    fun `macro parameter regex should not match invalid variable names`() {
        val input = "\$(invalid-name) \$(invalid.name)"
        val matches = macroParameterRegex.findAll(input).toList()

        // According to wiki, only [a-zA-Z0-9_] are valid
        assertEquals(0, matches.size, "Should not match variables with dashes or dots")
    }
}

