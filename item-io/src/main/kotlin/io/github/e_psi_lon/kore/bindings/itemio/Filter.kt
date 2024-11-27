package io.github.e_psi_lon.kore.bindings.itemio

import kotlinx.serialization.Serializable

@Serializable
data class Filter(
    val id: List<String>? = null,
    val ctx: List<String>? = null,
    val energy: Boolean? = null,
)