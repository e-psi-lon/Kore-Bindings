package io.github.e_psi_lon.kore.bindings.bookshelf.color

import io.github.ayfri.kore.arguments.types.DataArgument
import io.github.ayfri.kore.commands.function
import io.github.ayfri.kore.functions.Function
import io.github.e_psi_lon.kore.bindings.bookshelf.Bookshelf
import io.github.e_psi_lon.kore.bindings.core.Library
import io.github.e_psi_lon.kore.bindings.core.SupportedSource
import net.benwoodworth.knbt.StringifiedNbt
import net.benwoodworth.knbt.encodeToNbtTag
import net.benwoodworth.knbt.nbtCompound

object Color : Library {
    override val namespace: String
        get() = "${Bookshelf.namespace}.color"
    override val version: String
        get() = "2.1.1"
    override val source: SupportedSource
        get() = TODO("Not yet implemented")
    override val location: String
        get() = "color"

    context(Function)
    fun rgbToHex(r: Int, g: Int, b: Int) {
        function(
            namespace,
            "rgb_to_hex",
            true,
            StringifiedNbt.encodeToNbtTag(mapOf("color" to listOf(r, g, b))).nbtCompound
        )
    }

    context(Function)
    fun intToHex(i: Int) {
        function(namespace, "int_to_hex", true, StringifiedNbt.encodeToNbtTag(mapOf("color" to i)).nbtCompound)
    }

    context(Function)
    fun intToHex(path: String, source: DataArgument) {
        function(namespace, "int_to_hex", true, source, path)
    }

    context(Function)
    fun hexToInt(hex: String) {
        function(namespace, "hex_to_int", true, StringifiedNbt.encodeToNbtTag(mapOf("color" to hex)).nbtCompound)
    }

    context(Function)
    fun rgbToInt(r: Int, g: Int, b: Int) {
        function(
            namespace,
            "rgb_to_int",
            true,
            StringifiedNbt.encodeToNbtTag(mapOf("color" to listOf(r, g, b))).nbtCompound
        )
    }

    context(Function)
    fun hexToRgb(hex: String) {
        function(namespace, "hex_to_rgb", true, StringifiedNbt.encodeToNbtTag(mapOf("color" to hex)).nbtCompound)
    }

    context(Function)
    fun intToRgb(i: Int) {
        function(namespace, "int_to_rgb", true, StringifiedNbt.encodeToNbtTag(mapOf("color" to i)).nbtCompound)
    }

    context(Function)
    fun intToRgb(path: String, source: DataArgument) {
        function(namespace, "int_to_rgb", true, source, path)
    }


}

val Bookshelf.color
    get() = Color
