package io.github.e_psi_lon.kore.bindings.generation.poet

import com.squareup.kotlinpoet.*

internal class PropertyBuilder(
	name: String,
	type: TypeName,
) {
	private var builder = PropertySpec.builder(name, type)
	private var getterFunc: FunSpec.Builder? = null
	private var setterFunc: FunSpec.Builder? = null
	fun initializer(format: String, vararg args: Any) {
		builder = builder.initializer(format, *args)
	}

	fun initializer(block: CodeBlock.Builder.() -> Unit) {
		builder = builder.initializer(codeBlock(block))
	}

    fun delegate(memberName: MemberName, block: CodeBlock.Builder.() -> Unit) {
        delegate("%M { %L }", memberName, codeBlock(block))
    }

    fun delegate(format: String, vararg args: Any) {
        builder = builder.delegate(format, *args)
    }

	inline fun <reified T : Annotation> addAnnotation(
		noinline block: AnnotationSpec.Builder.() -> Unit = {}
	) {
		addAnnotation(T::class.asClassName(), block)
	}

	fun addAnnotation(
		type: ClassName,
		block: AnnotationSpec.Builder.() -> Unit = {}
	) {
		builder = builder.addAnnotation(AnnotationSpec.builder(type).apply(block).build())
	}

	fun addDocs(vararg docs: String) {
		builder = builder.addKdoc("%L", docs.joinToString("\n"))
	}

	fun addModifiers(vararg modifiers: KModifier) {
		builder = builder.addModifiers(*modifiers)
	}

	fun removeModifiers(vararg modifiers: KModifier) {
		builder.modifiers.removeAll(modifiers.toSet())
	}

	fun getter(block: FunSpec.Builder.() -> Unit) {
		getterFunc = getterFunc?.apply(block) ?: FunSpec.getterBuilder().apply(block)
	}

	fun setter(block: FunSpec.Builder.() -> Unit) {
		setterFunc = setterFunc?.apply(block) ?: FunSpec.setterBuilder().apply(block)
	}

	fun receiver(receiver: TypeName) {
		builder = builder.receiver(receiver)
	}

	fun build(): PropertySpec {
		return builder.apply {
			getterFunc?.let { getter(it.build()) }
            setterFunc?.let { setter(it.build()) }
		}.build()
	}
}
