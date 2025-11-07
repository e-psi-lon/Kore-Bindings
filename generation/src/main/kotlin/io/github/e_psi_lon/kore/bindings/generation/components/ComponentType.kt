package io.github.e_psi_lon.kore.bindings.generation.components

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asClassName
import io.github.ayfri.kore.utils.pascalCase

interface ComponentType {
    val name: String
    val directoryName: String
    val fileExtension: String
    val koreMethodOrClass: ClassOrMemberName
    val returnType: ClassName
    val requiredContext: ClassName?
    val parameters: Map<ParameterSpec, ParameterValueSource>
    val duplicateSuffix: String
        get() = name.pascalCase()

    companion object {
        fun usualParam(name: String = "name") = mapOf(
            ParameterSpec.builder(name, String::class).build() to ParameterValueSource.Name,
            ParameterSpec.builder("namespace", String::class).build() to ParameterValueSource.Namespace
        )

        inline fun <reified T> classOrMemberOf(): ClassOrMemberName = T::class.asClassName().toClassOrMemberName()
    }
}