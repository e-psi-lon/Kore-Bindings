package io.github.e_psi_lon.kore.bindings.generation

import io.github.ayfri.kore.utils.pascalCase


fun String.sanitizeCamel() = sanitizePascal().replaceFirstChar { if (!it.isLowerCase()) it.lowercase() else it.toString() }

fun String.sanitizePascal() = pascalCase()
    .replace('-', '_')
    .replace(".", "_")

