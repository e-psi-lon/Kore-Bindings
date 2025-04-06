package io.github.e_psi_lon.kore.bindings.energy

import io.github.ayfri.kore.arguments.numbers.ranges.IntRangeOrInt
import io.github.ayfri.kore.arguments.scores.Scores
import io.github.ayfri.kore.arguments.scores.SelectorScore
import io.github.ayfri.kore.arguments.scores.score
import io.github.ayfri.kore.arguments.types.literals.SelectorArgument
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


fun Energy.send(selector: SelectorArgument, reverse: Boolean = false): SelectorArgument {
    selector.selector.nbtData.apply {
        tag = if (reverse) {
            "$namespace.send"
        } else {
            !"$namespace.send"
        }
    }
    return selector
}

fun Energy.sender(selector: SelectorArgument): SelectorArgument {
    return receive(send(selector), true)
}

fun Energy.receive(selector: SelectorArgument, reverse: Boolean = false): SelectorArgument {
    selector.selector.nbtData.apply {
        tag = if (reverse) {
            "$namespace.receive"
        } else {
            !"$namespace.receive"
        }
    }
    return selector
}

fun Energy.receiver(selector: SelectorArgument): SelectorArgument {
    return receive(send(selector, true))
}

fun Energy.cable(selector: SelectorArgument, reverse: Boolean = false): SelectorArgument {
    selector.selector.nbtData.apply {
        tag = if (reverse) {
            "$namespace.cable"
        } else {
            !"$namespace.cable"
        }
    }
    return selector
}

fun Energy.senderAndReceiver(selector: SelectorArgument): SelectorArgument {
    return send(receive(selector))
}

@Serializable
@SerialName("energy")
data class EnergyData(val storage: Int, val maxStorage: Int)

context(Scores<SelectorScore>)
fun Energy.data() = score("$namespace.data")

context(Scores<SelectorScore>)
fun Energy.data(value: Int) = score("$namespace.data", value)

context(Scores<SelectorScore>)
fun Energy.data(value: IntRange) = score("$namespace.data", value)

context(Scores<SelectorScore>)
fun Energy.data(value: IntRangeOrInt) = score("$namespace.data", value)

context(Scores<SelectorScore>)
fun Energy.storage() = score("$namespace.storage")

context(Scores<SelectorScore>)
fun Energy.storage(value: Int) = score("$namespace.storage", value)

context(Scores<SelectorScore>)
fun Energy.storage(value: IntRange) = score("$namespace.storage", value)

context(Scores<SelectorScore>)
fun Energy.storage(value: IntRangeOrInt) = score("$namespace.storage", value)

context(Scores<SelectorScore>)
fun Energy.maxStorage() = score("$namespace.max_storage")

context(Scores<SelectorScore>)
fun Energy.maxStorage(value: Int) = score("$namespace.max_storage", value)

context(Scores<SelectorScore>)
fun Energy.maxStorage(value: IntRange) = score("$namespace.max_storage", value)

context(Scores<SelectorScore>)
fun Energy.maxStorage(value: IntRangeOrInt) = score("$namespace.max_storage", value)

context(Scores<SelectorScore>)
fun Energy.transferRate() = score("$namespace.transfer_rate")

context(Scores<SelectorScore>)
fun Energy.transferRate(value: Int) = score("$namespace.transfer_rate", value)

context(Scores<SelectorScore>)
fun Energy.transferRate(value: IntRange) = score("$namespace.transfer_rate", value)

context(Scores<SelectorScore>)
fun Energy.transferRate(value: IntRangeOrInt) = score("$namespace.transfer_rate", value)

context(Scores<SelectorScore>)
fun Energy.changeRate() = score("$namespace.change_rate")

context(Scores<SelectorScore>)
fun Energy.changeRate(value: Int) = score("$namespace.change_rate", value)

context(Scores<SelectorScore>)
fun Energy.changeRate(value: IntRange) = score("$namespace.change_rate", value)

context(Scores<SelectorScore>)
fun Energy.changeRate(value: IntRangeOrInt) = score("$namespace.change_rate", value)