package io.github.e_psi_lon.kore.bindings.energy

import io.github.ayfri.kore.arguments.numbers.ranges.IntRangeOrInt
import io.github.ayfri.kore.arguments.scores.Scores
import io.github.ayfri.kore.arguments.scores.SelectorScore
import io.github.ayfri.kore.arguments.scores.score
import io.github.ayfri.kore.arguments.types.literals.SelectorArgument
import io.github.ayfri.kore.commands.function
import io.github.ayfri.kore.functions.Function
import io.github.e_psi_lon.kore.bindings.core.Library
import io.github.e_psi_lon.kore.bindings.core.SupportedSource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object Energy: Library {
    override val namespace: String = "energy"
    override val version: String
        get() = ""
    override val source: SupportedSource
        get() = SupportedSource.DOWNLOAD
    override val location: String
        get() = ""

    val api = Api

    fun send(selector: SelectorArgument, reverse: Boolean = false): SelectorArgument {
        selector.selector.nbtData.apply {
            tag = if (reverse) {
                "$namespace.send"
            } else {
                !"$namespace.send"
            }
        }
        return selector
    }

    fun sender(selector: SelectorArgument): SelectorArgument {
        selector.selector.nbtData.apply {
            tag = "$namespace.send"
            tag = !"$namespace.receive"
        }
        return selector
    }

    fun receive(selector: SelectorArgument, reverse: Boolean = false): SelectorArgument {
        selector.selector.nbtData.apply {
            tag = if (reverse) {
                "$namespace.receive"
            } else {
                !"$namespace.receive"
            }
        }
        return selector
    }

    fun receiver(selector: SelectorArgument): SelectorArgument {
        selector.selector.nbtData.apply {
            tag = "$namespace.receive"
            tag = !"$namespace.send"
        }
        return selector
    }

    fun cable(selector: SelectorArgument, reverse: Boolean = false): SelectorArgument {
        selector.selector.nbtData.apply {
            tag = if (reverse) {
                "$namespace.cable"
            } else {
                !"$namespace.cable"
            }
        }
        return selector
    }

    fun senderAndReceiver(selector: SelectorArgument): SelectorArgument {
        selector.selector.nbtData.apply {
            tag = "$namespace.send"
            tag = "$namespace.receive"
        }
        return selector
    }

    context(Function)
    fun cableCanConnect() = function(namespace, "v1/cable_can_connect", true)

    context(Function)
    fun cableUpdate() = function(namespace, "v1/cable_update", true)

    context(Function)
    fun energyUpdate() = function(namespace, "v1/energy_update",  true)

    context(Function)
    fun updateEnergyItem() = function("namespace, v1/update_energy_item", true)

    @Serializable
    @SerialName("energy")
    data class EnergyData(val storage: Int, val maxStorage: Int)

    context(Scores<SelectorScore>)
    fun data() = score("$namespace.data")

    context(Scores<SelectorScore>)
    fun data(value: Int) = score("$namespace.data", value)

    context(Scores<SelectorScore>)
    fun data(value: IntRange) = score("$namespace.data", value)

    context(Scores<SelectorScore>)
    fun data(value: IntRangeOrInt) = score("$namespace.data", value)

    context(Scores<SelectorScore>)
    fun storage() = score("$namespace.storage")

    context(Scores<SelectorScore>)
    fun storage(value: Int) = score("$namespace.storage", value)

    context(Scores<SelectorScore>)
    fun storage(value: IntRange) = score("$namespace.storage", value)

    context(Scores<SelectorScore>)
    fun storage(value: IntRangeOrInt) = score("$namespace.storage", value)

    context(Scores<SelectorScore>)
    fun maxStorage() = score("$namespace.max_storage")

    context(Scores<SelectorScore>)
    fun maxStorage(value: Int) = score("$namespace.max_storage", value)

    context(Scores<SelectorScore>)
    fun maxStorage(value: IntRange) = score("$namespace.max_storage", value)

    context(Scores<SelectorScore>)
    fun maxStorage(value: IntRangeOrInt) = score("$namespace.max_storage", value)

    context(Scores<SelectorScore>)
    fun transferRate() = score("$namespace.transfer_rate")

    context(Scores<SelectorScore>)
    fun transferRate(value: Int) = score("$namespace.transfer_rate", value)

    context(Scores<SelectorScore>)
    fun transferRate(value: IntRange) = score("$namespace.transfer_rate", value)

    context(Scores<SelectorScore>)
    fun transferRate(value: IntRangeOrInt) = score("$namespace.transfer_rate", value)

    context(Scores<SelectorScore>)
    fun changeRate() = score("$namespace.change_rate")

    context(Scores<SelectorScore>)
    fun changeRate(value: Int) = score("$namespace.change_rate", value)

    context(Scores<SelectorScore>)
    fun changeRate(value: IntRange) = score("$namespace.change_rate", value)

    context(Scores<SelectorScore>)
    fun changeRate(value: IntRangeOrInt) = score("$namespace.change_rate", value)
}