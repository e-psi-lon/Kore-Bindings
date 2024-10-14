package io.github.e_psi_lon.kore.bindings.bookshelf.block

import io.github.ayfri.kore.arguments.maths.Vec3
import io.github.ayfri.kore.commands.Command
import io.github.ayfri.kore.commands.data
import io.github.ayfri.kore.commands.execute.execute
import io.github.ayfri.kore.commands.function
import io.github.ayfri.kore.functions.Function
import io.github.e_psi_lon.kore.bindings.bookshelf.Bookshelf
import io.github.e_psi_lon.kore.bindings.core.Library
import io.github.e_psi_lon.kore.bindings.core.SupportedSource
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.StringifiedNbt
import net.benwoodworth.knbt.encodeToNbtTag

object Block : Library {
    override val namespace = "${Bookshelf.namespace}.block"
    override val version: String
        get() = "2.1.1"
    override val source: SupportedSource
        get() = TODO("Not yet implemented")
    override val location: String
        get() = "block"

    context(Function)
    fun fillBlock(fillData: FillBlockData) {
        data(Bookshelf.inStorage()) {
            modify("block.fill_block", StringifiedNbt { nameRootClasses = false }.encodeToNbtTag(fillData))
        }
        function(namespace, "fill_block", true)
    }

    context(Function)
    fun fillType(fillData: FillTypeData) {
        data(Bookshelf.inStorage()) {
            modify("block.fill_type", StringifiedNbt { nameRootClasses = false }.encodeToNbtTag(fillData))
        }
        function(namespace, "fill_type", true)
    }

    context(Function)
    fun getBlock(pos: Vec3) {
        execute {
            positioned(pos)
            run {
                function(namespace, "get_block", true)
            }
        }
    }

    context(Function)
    fun getType(pos: Vec3) {
        execute {
            positioned(pos)
            run {
                function(namespace, "get_types", true)
            }
        }
    }

    context(Function)
    fun keepBlockProperties(pos: Vec3, properties: List<Map<String, String>>) {
        getBlock(pos)
        function(
            namespace,
            "keep_properties",
            true,
            NbtCompound(mapOf("properties" to StringifiedNbt.encodeToNbtTag(properties)))
        )
    }

    context(Function)
    fun mergeBlockProperties(pos1: Vec3, pos2: Vec3, properties: List<Map<String, String>>) {
        getBlock(pos1)
        execute {
            positioned(pos2)
            run {
                function(
                    namespace,
                    "merge_properties",
                    true,
                    NbtCompound(mapOf("properties" to StringifiedNbt.encodeToNbtTag(properties)))
                )
            }
        }
    }

    context(Function)
    fun removeBlockProperties(pos: Vec3, properties: List<Map<String, String>>) {
        getBlock(pos)
        function(
            namespace,
            "remove_properties",
            true,
            NbtCompound(mapOf("properties" to StringifiedNbt.encodeToNbtTag(properties)))
        )
    }

    context(Function)
    fun replaceBlockProperties(pos: Vec3, properties: List<Map<String, String>>) {
        getBlock(pos)
        function(
            namespace,
            "replace_properties",
            true,
            NbtCompound(mapOf("properties" to StringifiedNbt.encodeToNbtTag(properties)))
        )
    }

    context(Function)
    fun shiftBlockProperties(pos1: Vec3) {
        getBlock(pos1)
        function(namespace, "shift_properties", true)
    }

    context(Function)
    fun replaceType(pos: Vec3, type: String) {
        getBlock(pos)
        function(namespace, "replace_type", true, NbtCompound(mapOf("type" to StringifiedNbt.encodeToNbtTag(type))))
    }

    context(Function)
    fun mapType(
        pos: Vec3,
        type: String,
        namespace: String,
        mapping: List<Map<String, String>>,
        mappingName: String? = null
    ) {
        getBlock(pos)
        val path = if (mappingName == null) "$namespace.${mapping.hashCode()}" else "$namespace.$mappingName"
        data(Bookshelf.constStorage()) {
            modify("block.mapping_registry.$path", StringifiedNbt { nameRootClasses = false }.encodeToNbtTag(mapping))
        }
        function(
            namespace,
            "map_type",
            true,
            NbtCompound(
                mapOf(
                    "type" to StringifiedNbt.encodeToNbtTag(type),
                    "mapping_registry" to StringifiedNbt.encodeToNbtTag(path)
                )
            )
        )
    }

    context(Function)
    fun match(pos1: Vec3, pos2: Vec3, onMatch: Function.() -> Command) {
        getBlock(pos1)
        data(Bookshelf.inStorage()) {
            modify("block.match.block", Bookshelf.outStorage(), "block.block")
        }
        execute {
            positioned(pos2)
            run(onMatch)
        }
    }

    context(Function)
    fun matchType(pos1: Vec3, pos2: Vec3, onMatch: Function.() -> Command) {
        getType(pos1)
        data(Bookshelf.inStorage()) {
            modify("block.match.type", Bookshelf.outStorage(), "block.type")
        }
        execute {
            positioned(pos2)
            run(onMatch)
        }
    }

    context(Function)
    fun setBlock(pos: Vec3, data: SetBlockData) {
        data(Bookshelf.inStorage()) {
            modify("block.set_block", StringifiedNbt { nameRootClasses = false }.encodeToNbtTag(data))
        }
        execute {
            positioned(pos)
            run {
                function(namespace, "set_block", true)
            }
        }
    }

    context(Function)
    fun setType(pos: Vec3, data: SetTypeData) {
        data(Bookshelf.inStorage()) {
            modify("block.set_type", StringifiedNbt { nameRootClasses = false }.encodeToNbtTag(data))
        }
        execute {
            positioned(pos)
            run {
                function(namespace, "set_type", true)
            }
        }
    }

    context(Function)
    fun spawnBlockDisplay(orignalPos: Vec3) {
        getBlock(orignalPos)
        data(Bookshelf.inStorage()) {
            modify("block.spawn_block_display", Bookshelf.outStorage(), "block.block")
        }
        function(namespace, "spawn_block_display", true)
    }

    context(Function)
    fun spawnBlockDisplay(data: BlockData) {
        data(Bookshelf.inStorage()) {
            modify("block.spawn_block_display", StringifiedNbt { nameRootClasses = false }.encodeToNbtTag(data))
        }
        function(namespace, "spawn_block_display", true)
    }

    context(Function)
    fun spawnFallingBlock(orignalPos: Vec3) {
        getBlock(orignalPos)
        data(Bookshelf.inStorage()) {
            modify("block.spawn_falling_block", Bookshelf.outStorage(), "block.block")
        }
        function(namespace, "spawn_falling_block", true)
    }

    context(Function)
    fun spawnFallingBlock(data: BlockData) {
        data(Bookshelf.inStorage()) {
            modify("block.spawn_falling_block", StringifiedNbt { nameRootClasses = false }.encodeToNbtTag(data))
        }
        function(namespace, "spawn_falling_block", true)
    }

    context(Function)
    fun spawnSolidBlockDisplay(orignalPos: Vec3) {
        getBlock(orignalPos)
        data(Bookshelf.inStorage()) {
            modify("block.spawn_solid_block_display", Bookshelf.outStorage(), "block.block")
        }
        function(namespace, "spawn_solid_block_display", true)
    }

    context(Function)
    fun spawnSolidBlockDisplay(data: BlockData) {
        data(Bookshelf.inStorage()) {
            modify("block.spawn_solid_block_display", StringifiedNbt { nameRootClasses = false }.encodeToNbtTag(data))
        }
        function(namespace, "spawn_solid_block_display", true)
    }

    context(Function)
    fun setCustomRegistry(namespace: String, name: String, registry: List<MappingRegistry>) {
        data(Bookshelf.constStorage()) {
            modify(
                "block.mapping_registry.$namespace.$name",
                StringifiedNbt { nameRootClasses = false }.encodeToNbtTag(registry)
            )
        }
    }
}

val Bookshelf.block
    get() = Block