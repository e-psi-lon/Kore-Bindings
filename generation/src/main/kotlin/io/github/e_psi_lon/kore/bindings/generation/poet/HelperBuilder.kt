package io.github.e_psi_lon.kore.bindings.generation.poet

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.typeNameOf
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

fun FunSpec.Builder.addDocs(vararg docs: String): FunSpec.Builder =
    addKdoc("%L", docs.joinToString("\n"))

inline fun <reified T> FunSpec.Builder.addParameter(name: String, block: ParameterSpec.Builder.() -> Unit = {}): FunSpec.Builder {
    val builder = ParameterSpec.builder(name, typeNameOf<T>()).apply(block)
    return addParameter(builder.build())
}

fun KFunction<*>.asMemberName(): MemberName {
    val javaMethod = this.javaMethod ?: error("Function has no Java method: $this")
    val declaringClass = javaMethod.declaringClass
    val packageName = declaringClass.`package`?.name ?: error("Cannot determine package")
    val isTopLevel = declaringClass.simpleName.endsWith("Kt")
    return if (isTopLevel) {
        MemberName(packageName, this.name)
    } else {
        val rawClassName = declaringClass.name.removePrefix("$packageName.").split('.')
        val className = ClassName(packageName, rawClassName.first(), *rawClassName.drop(1).toTypedArray())
        className.member(this.name)
    }
}

fun codeBlock(builder: CodeBlock.Builder.() -> Unit) =
    CodeBlock.builder().apply(builder).build()