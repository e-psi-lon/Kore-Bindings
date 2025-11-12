package io.github.e_psi_lon.kore.bindings.generation.poet

import com.squareup.kotlinpoet.*

fun PropertySpec.Builder.initializer(block: CodeBlock.Builder.() -> Unit) = initializer(codeBlock(block))
fun PropertySpec.Builder.delegate(typeMemberName: MemberName, block: CodeBlock.Builder.() -> Unit) =
	delegate("%M { %L }", typeMemberName, codeBlock(block))

inline fun <reified T : Annotation> PropertySpec.Builder.addAnnotation(
	noinline block: AnnotationSpec.Builder.() -> Unit = {}
) = addAnnotation(T::class.asClassName(), block)

fun PropertySpec.Builder.addAnnotation(
	type: ClassName,
	block: AnnotationSpec.Builder.() -> Unit = {}
) = addAnnotation(AnnotationSpec.builder(type).apply(block).build())

fun PropertySpec.Builder.getter(block: FunSpec.Builder.() -> Unit) =
	getter(FunSpec.getterBuilder().apply(block).build())

fun PropertySpec.Builder.addDocs(vararg docs: String) =
	addKdoc("%L", docs.joinToString("\n"))
