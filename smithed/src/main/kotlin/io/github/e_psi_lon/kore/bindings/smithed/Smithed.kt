package io.github.e_psi_lon.kore.bindings.smithed

import io.github.ayfri.kore.arguments.scores.Scores
import io.github.ayfri.kore.arguments.scores.SelectorScore
import io.github.ayfri.kore.arguments.scores.score
import io.github.ayfri.kore.arguments.types.ScoreHolderArgument
import io.github.ayfri.kore.commands.execute.ExecuteCondition
import io.github.ayfri.kore.commands.execute.ExecuteStore

object Smithed {
    val namespace: String = "smithed"

    context(Scores<SelectorScore>)
    fun data() = score("${namespace}.data")

    fun data() = "${namespace}.data"

    context(ExecuteStore)
    fun data(target: ScoreHolderArgument) = score(target, "${namespace}.data")

    context(ExecuteCondition)
    fun data(target: ScoreHolderArgument) = score(target, "${namespace}.data")
}