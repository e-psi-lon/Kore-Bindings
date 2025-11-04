package io.github.e_psi_lon.kore.bindings.generation.poet

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

fun FunSpec.Builder.addDocs(vararg docs: String): FunSpec.Builder {
    return addKdoc("%L", docs.joinToString("\n"))
}

inline fun <reified T> FunSpec.Builder.addParameter(name: String, block: ParameterSpec.Builder.() -> Unit = {}): FunSpec.Builder {
    val builder = ParameterSpec.builder(name, T::class.asTypeName()).apply(block)
    return addParameter(builder.build())
}


fun KFunction<*>.asMemberName() = MemberName(
    this.javaMethod!!.declaringClass.`package`.name,
    this.name
)