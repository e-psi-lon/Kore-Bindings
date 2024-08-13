package io.github.e_psi_lon.kore.bindings.bookshelf.id

import io.github.ayfri.kore.arguments.types.literals.literal
import io.github.ayfri.kore.arguments.types.resources.PredicateArgument
import io.github.ayfri.kore.commands.function
import io.github.ayfri.kore.functions.Function
import io.github.e_psi_lon.kore.bindings.bookshelf.Bookshelf
import io.github.e_psi_lon.kore.bindings.core.Library
import io.github.e_psi_lon.kore.bindings.core.SupportedSource

object Id : Library {
    override val namespace = "${Bookshelf.namespace}.id"
    override val version: String
        get() = "2.1.1"
    override val source: SupportedSource
        get() = TODO("Not yet implemented")
    override val location: String
        get() = "id"

    fun sUId() = literal("\$id.suid")
    fun cUId() = literal("\$id.cuid")
    fun sUIdMin() = literal("\$id.suid.min")
    fun sUIDMax() = literal("\$id.suid.max")
    fun cUIdMin() = literal("\$id.cuid.min")
    fun cUIDMax() = literal("\$id.cuid.max")

    fun hasSUID() = PredicateArgument("has_suid", namespace)
    fun hasCUId() = PredicateArgument("has_cuid", namespace)
    fun sUIdEqual() = PredicateArgument("suid_equal", namespace)
    fun cUIdEqual() = PredicateArgument("cuid_equal", namespace)
    fun sUIdLower() = PredicateArgument("suid_lower", namespace)
    fun cUIdLower() = PredicateArgument("cuid_lower", namespace)
    fun sUIdUpper() = PredicateArgument("suid_upper", namespace)
    fun cUIdUpper() = PredicateArgument("cuid_upper", namespace)
    fun sUIdMatch() = PredicateArgument("suid_match", namespace)
    fun cUIdMatch() = PredicateArgument("cuid_match", namespace)

    context(Function)
    fun giveSUId() {
        function(namespace, "give_suid", true)
    }

    context(Function)
    fun giveCUId() {
        function(namespace, "give_cuid", true)
    }

    context(Function)
    fun updateCUIds() {
        function(namespace, "update_cuids", true)

    }
}

val Bookshelf.id
    get() = Id