package io.github.e_psi_lon.kore.bindings.generation.components

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import io.github.ayfri.kore.utils.pascalCase


interface ComponentType {
    val name: String
    val directoryName: String
    val fileExtension: String
        // The default value is "json" because it's the most common case.
        get() = "json"
    val koreMethodOrClass: ClassOrMemberName
    val returnType: ClassName
        get() = when (koreMethodOrClass) {
            // Smart cast to 'ClassOrMemberName.Class' is impossible, because 'koreMethodOrClass' is a property that has
            // an open or custom getter even within the branch. Manual cast was suggested by the compiler itself
            is ClassOrMemberName.Class -> (koreMethodOrClass as ClassOrMemberName.Class).name
            // MemberName is the type wrapped by ClassOrMemberName.Member
            is ClassOrMemberName.Member -> throw IllegalStateException("returnType must be overridden because koreMethodOrClass is meant to represent a MemberName")
        }
    val requiredContext: ClassName? get() = null
    val parameters: Map<ParameterSpec, ParameterValueSource>
        // The default value is namespace and name to null because it's the most common case.
        get() = usualParam()
    val duplicateSuffix: String
        get() = name.pascalCase()

    fun usualParam(name: String = "name") = mapOf(
        ParameterSpec.builder(name, String::class).build() to ParameterValueSource.Name,
        ParameterSpec.builder("namespace", String::class).build() to ParameterValueSource.Namespace
    )
}