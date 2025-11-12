package io.github.e_psi_lon.kore.bindings.generation.components

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import io.github.ayfri.kore.utils.pascalCase

interface ComponentType {
    val name: String
    val directoryName: String
    val fileExtension: String
    val koreMethodOrClass: ClassOrMemberName
    val returnType: ClassName
    val requiredContext: ClassName?
    val parameters: Map<String, ParameterValueSource>
    val duplicateSuffix: String
        get() = name.lowercase().pascalCase()
    val humanReadableName: String
        get() = name.lowercase().replace('_', ' ').capitalize()

    companion object {
        fun usualParam(name: String = "name") = mapOf(
            name to ParameterValueSource.Name,
            "namespace" to ParameterValueSource.Namespace
        )

        inline fun <reified T> classOrMemberOf(): ClassOrMemberName = T::class.asClassName().toClassOrMemberName()
    }
}