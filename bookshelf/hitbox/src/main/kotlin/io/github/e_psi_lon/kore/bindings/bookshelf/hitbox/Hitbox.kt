package io.github.e_psi_lon.kore.bindings.bookshelf.hitbox

import io.github.ayfri.kore.commands.function
import io.github.ayfri.kore.functions.Function
import io.github.e_psi_lon.kore.bindings.bookshelf.Bookshelf
import io.github.e_psi_lon.kore.bindings.core.Library
import io.github.e_psi_lon.kore.bindings.core.SupportedSource

object Hitbox : Library {
    override val namespace: String
        get() = "${Bookshelf.namespace}.hitbox"
    override val version: String
        get() = "2.1.1"
    override val source: SupportedSource
        get() = TODO("Not yet implemented")
    override val url: String
        get() = "hitbox"

    context(Function)
    fun getBlock() {
        function("get_block", namespace, true)
    }

    context(Function)
    fun getEntity() {
        function("get_entity", namespace, true)
    }

    context(Function)
    fun isInBlock() {
        function("is_in_block", namespace, true)
    }

    context(Function)
    fun isInEntity() {
        function("is_in_entity", namespace, true)
    }
}

val Bookshelf.hitbox
    get() = Hitbox