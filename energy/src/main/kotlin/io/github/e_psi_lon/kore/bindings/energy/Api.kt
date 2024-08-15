package io.github.e_psi_lon.kore.bindings.energy

import io.github.ayfri.kore.functions.Function
import io.github.ayfri.kore.commands.function

object Api {
    context(Function)
    fun initCable() = function(Energy.namespace, "v1/api/init_cable")

    context(Function)
    fun initMachine() = function(Energy.namespace,"v1/api/init_machine")

    context(Function)
    fun breakCable() = function(Energy.namespace,"v1/api/break_cable")

    context(Function)
    fun breakMachine() = function(Energy.namespace, "v1/api/break_machine")

    context(Function)
    fun modifyPlayerEnergy() = function(Energy.namespace,"v1/api/modify_player_energy")
}