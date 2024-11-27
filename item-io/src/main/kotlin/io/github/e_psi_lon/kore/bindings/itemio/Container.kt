package io.github.e_psi_lon.kore.bindings.itemio

import io.github.ayfri.kore.arguments.types.literals.SelectorArgument

object Container {
    fun nope(selector: SelectorArgument, reverse: Boolean = false): SelectorArgument {
        selector.selector.nbtData.apply {
            tag = if (reverse) {
                "${ItemIO.namespace}.nope"
            } else {
                !"${ItemIO.namespace}.nope"
            }
        }
        return selector
    }

    fun hopper(selector: SelectorArgument, reverse: Boolean = false): SelectorArgument {
        selector.selector.nbtData.apply {
            tag = if (reverse) {
                "${ItemIO.namespace}.hopper"
            } else {
                !"${ItemIO.namespace}.hopper"
            }
        }
        return selector
    }

    fun notVanillaContainer(selector: SelectorArgument, reverse: Boolean = false): SelectorArgument {
        selector.selector.nbtData.apply {
            tag = if (reverse) {
                "${ItemIO.namespace}.not_vanilla_container"
            } else {
                !"${ItemIO.namespace}.not_vanilla_container"
            }
        }
        return selector
    }

    fun autoHandledIO(selector: SelectorArgument, reverse: Boolean = false): SelectorArgument {
        selector.selector.nbtData.apply {
            tag = if (reverse) {
                "${ItemIO.namespace}.auto_handled_io"
            } else {
                !"${ItemIO.namespace}.auto_handled_io"
            }
        }
        return selector
    }

    fun hopperProtection(selector: SelectorArgument, offset: Int, reverse: Boolean = false): SelectorArgument {
        if (offset < 0 || offset > 20) {
            throw IllegalArgumentException("Offset must be between 0 and 20.")
        }
        selector.selector.nbtData.apply {
            tag = if (reverse) {
                "${ItemIO.namespace}.hopper_protection_$offset"
            } else {
                !"${ItemIO.namespace}.hopper_protection_$offset"

            }
            return selector
        }
    }

    fun ioconfigFromStorage(selector: SelectorArgument, reverse: Boolean = false): SelectorArgument {
        selector.selector.nbtData.apply {
            tag = if (reverse) {
                "${ItemIO.namespace}.ioconfig_from_storage"
            } else {
                !"${ItemIO.namespace}.ioconfig_from_storage"
            }
        }
        return selector
    }

    fun nbtItems(selector: SelectorArgument, reverse: Boolean = false): SelectorArgument {
        selector.selector.nbtData.apply {
            tag = if (reverse) {
                "${ItemIO.namespace}.nbt_items"
            } else {
                !"${ItemIO.namespace}.nbt_items"
            }
        }
        return selector
    }

    fun nbtItemsOnPassengers(selector: SelectorArgument, reverse: Boolean = false): SelectorArgument {
        selector.selector.nbtData.apply {
            tag = if (reverse) {
                "${ItemIO.namespace}.nbt_items_on_passengers"
            } else {
                !"${ItemIO.namespace}.nbt_items_on_passengers"
            }
        }
        return selector
    }

    fun nbtItemsOnVehicle(selector: SelectorArgument, reverse: Boolean = false): SelectorArgument {
        selector.selector.nbtData.apply {
            tag = if (reverse) {
                "${ItemIO.namespace}.nbt_items_on_vehicle"
            } else {
                !"${ItemIO.namespace}.nbt_items_on_vehicle"
            }
        }
        return selector
    }
}