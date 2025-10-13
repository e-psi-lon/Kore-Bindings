package io.github.e_psi_lon.kore.bindings.generation.data


/**
 * Represents a scoreboard objective found in function files
 */
data class Scoreboard(
    override val name: String,  // "bs.ctx" or "myobjective"
    val sourceNamespace: String  // Which parsed namespace declared this
) : NamedResource {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Scoreboard) return false
        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()
}