package io.github.e_psi_lon.kore.bindings.generation.poet

import com.squareup.kotlinpoet.*

internal class PropertyBuilder(
	name: String,
	type: ClassName,
) {
	private var builder = PropertySpec.builder(name, type)
	private lateinit var getterFunc: FunSpec.Builder
	private lateinit var setterFunc: FunSpec.Builder
	fun initializer(format: String, vararg args: Any) {
		builder = builder.apply {
			this.initializer(format, *args)
		}
	}

	fun addModifiers(vararg modifiers: KModifier) {
		builder = builder.addModifiers(*modifiers)
	}

	fun removeModifiers(vararg modifiers: KModifier) {
		builder.modifiers.removeAll(modifiers.toSet())
	}

	fun getter(block: FunSpec.Builder.() -> Unit) {
		getterFunc = if (this::getterFunc.isInitialized) {
			getterFunc.apply(block)
		} else {
			FunSpec.getterBuilder().apply(block)
		}
	}

	fun setter(block: FunSpec.Builder.() -> Unit) {
		setterFunc = if (this::setterFunc.isInitialized) {
			setterFunc.apply(block)
		} else {
			FunSpec.setterBuilder().apply(block)
		}
	}

	fun receiver(receiver: TypeName) {
		builder = builder.receiver(receiver)
	}

	fun build(): PropertySpec {
		return builder.apply {
			if (this@PropertyBuilder::getterFunc.isInitialized) {
				getter(getterFunc.build())
			}
			if (this@PropertyBuilder::setterFunc.isInitialized) {
				setter(setterFunc.build())
			}
		}.build()
	}
}
