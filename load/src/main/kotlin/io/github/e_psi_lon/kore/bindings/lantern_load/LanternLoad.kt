package io.github.e_psi_lon.kore.bindings.lantern_load

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.numbers.ranges.IntRangeOrInt
import io.github.ayfri.kore.arguments.types.literals.literal
import io.github.ayfri.kore.commands.execute.execute
import io.github.ayfri.kore.commands.scoreboard.scoreboard
import io.github.ayfri.kore.features.tags.functionTag
import io.github.ayfri.kore.functions.Function
import io.github.ayfri.kore.functions.function
import io.github.e_psi_lon.kore.bindings.core.Library
import io.github.e_psi_lon.kore.bindings.core.SupportedSource

object LanternLoad : Library {
    override val namespace: String
        get() = "load"
    override val version: String
        get() = "unspecified"
    override val source: SupportedSource
        get() = SupportedSource.DOWNLOAD
    override val location: String
        get() = ""

    fun status() = "${namespace}.status"

    private fun DataPack.addLoadFunction(
        tagName: String,
        namespace: String,
        name: String? = null,
        directory: String? = null,
        function: Function.() -> Unit
    ) {
        if (directory != null && name == null) {
            throw IllegalArgumentException("If you specify a directory, you must specify a name.")
        }
        val generatedFunction = if (name == null) {
            val generatedFunction = Function("", "", "", this).apply(function)
            val hash = generatedFunction.hashCode()
            addGeneratedFunction(
                Function(
                    "generated_${hash}",
                    namespace,
                    datapack = this
                ).apply(function)
            )
        } else {
            function(name, namespace, directory!!, function)
        }
        functionTag(
            tagName,
            this@LanternLoad.namespace,
            false,
        ) {
            this += generatedFunction
        }
    }

    fun DataPack.preLoad(
        namespace: String,
        name: String? = null,
        directory: String? = null,
        function: Function.() -> Unit
    ) {
        addLoadFunction(
            "pre_load",
            namespace,
            name,
            directory,
            function
        )
    }

    fun DataPack.load(
        namespace: String,
        name: String? = null,
        directory: String? = null,
        function: Function.() -> Unit
    ) {
        addLoadFunction(
            "load",
            namespace,
            name,
            directory,
            function
        )
    }

    context(DataPack)
    fun postLoad(
        namespace: String,
        name: String? = null,
        directory: String? = null,
        function: Function.() -> Unit
    ) {
        addLoadFunction(
            "post_load",
            namespace,
            name,
            directory,
            function
        )
    }

    context(Function)
    fun setStatus(status: Int) = scoreboard {
        players {
            add(literal(datapack.name), status(), status)
        }
    }

    context(Function)
    fun checkDependenciesBeforeLoad(version: Int, vararg dependencies: Pair<String, Int>) = execute {
        dependencies.forEach { (dependency, version) ->
            ifCondition {
                score(literal(dependency), status(), IntRangeOrInt(int = version))
            }
        }

        run {
            setStatus(version)
        }
    }
}