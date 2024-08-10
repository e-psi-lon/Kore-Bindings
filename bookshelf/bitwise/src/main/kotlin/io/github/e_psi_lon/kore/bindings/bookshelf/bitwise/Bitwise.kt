package io.github.e_psi_lon.kore.bindings.bookshelf.bitwise

import io.github.ayfri.kore.arguments.types.literals.literal
import io.github.ayfri.kore.commands.function
import io.github.ayfri.kore.commands.scoreboard.scoreboard
import io.github.ayfri.kore.functions.Function
import io.github.e_psi_lon.kore.bindings.bookshelf.Bookshelf
import io.github.e_psi_lon.kore.bindings.core.Library
import io.github.e_psi_lon.kore.bindings.core.SupportedSource

object Bitwise : Library {
    override val namespace: String = "${Bookshelf.namespace}.bitwise"
    override val version: String
        get() = "2.1.1"
    override val source: SupportedSource
        get() = TODO("Not yet implemented") // SupportedSource.BOOKSHELF not yet implemented
    override val url: String
        get() = "bitwise"

    context(Function)
    fun bitCount(number: Int) {
        scoreboard {
            players {
                set(literal("\$bitwise.bit_count.n"), Bookshelf.inScoreboard(), number)
            }
        }
        function("bit_count", namespace, true)
    }

    context(Function)
    fun bitLength(number: Int) {
        scoreboard {
            players {
                set(literal("\$bitwise.bit_length.n"), Bookshelf.inScoreboard(), number)
            }
        }
        function("bit_length", namespace, true)
    }

    context(Function)
    fun twoComplement(number: Int) {
        scoreboard {
            players {
                set(literal("\$bitwise.two_complement.n"), Bookshelf.inScoreboard(), number)
            }
        }
        function("two_complement", namespace, true)
    }

    context(Function)
    fun bitAnd(a: Int, b: Int) {
        scoreboard {
            players {
                set(literal("\$bitwise.and.a"), Bookshelf.inScoreboard(), a)
                set(literal("\$bitwise.and.b"), Bookshelf.inScoreboard(), b)
            }
        }
        function("and", namespace, true)
    }

    context(Function)
    fun bitOr(a: Int, b: Int) {
        scoreboard {
            players {
                set(literal("\$bitwise.or.a"), Bookshelf.inScoreboard(), a)
                set(literal("\$bitwise.or.b"), Bookshelf.inScoreboard(), b)
            }
        }
        function("or", namespace, true)
    }

    context(Function)
    fun bitXor(a: Int, b: Int) {
        scoreboard {
            players {
                set(literal("\$bitwise.xor.a"), Bookshelf.inScoreboard(), a)
                set(literal("\$bitwise.xor.b"), Bookshelf.inScoreboard(), b)
            }
        }
        function("xor", namespace, true)
    }

    context(Function)
    fun bitNot(number: Int) {
        scoreboard {
            players {
                set(literal("\$bitwise.not.n"), Bookshelf.inScoreboard(), number)
            }
        }
        function("not", namespace, true)
    }

}

val Bookshelf.bitwise
    get() = Bitwise