package io.github.e_psi_lon.kore.bindings.generation.poet

import com.squareup.kotlinpoet.*

internal class TypeBuilder(
	name: String,
	type: TypeSpec.Kind
) {
	private var builder = when (type) {
		TypeSpec.Kind.INTERFACE -> TypeSpec.interfaceBuilder(name)
		TypeSpec.Kind.CLASS -> TypeSpec.classBuilder(name)
		TypeSpec.Kind.OBJECT -> TypeSpec.objectBuilder(name)
	}
	val properties = mutableMapOf<String, PropertyBuilder>()
	val functions = mutableMapOf<String, FunSpec.Builder>()
	val typeSpecs = mutableMapOf<String, TypeBuilder>()

	fun build(): TypeSpec {
		return builder.apply {
			properties.forEach { (_, builder) ->
				addProperty(builder.build())
			}
			functions.forEach { (_, builder) ->
				addFunction(builder.build())
			}
			this@TypeBuilder.typeSpecs.forEach { (_, builder) ->
				addType(builder.build())
			}
		}.build()
	}

	fun superclass(name: ClassName) {
		builder = builder.superclass(name)
	}

	inline fun <reified T : Annotation> addAnnotation(noinline block: AnnotationSpec.Builder.() -> Unit = {}) {
		builder.addAnnotation(AnnotationSpec.builder(T::class).apply(block).build())
	}

	inline fun <reified T : Any> property(name: String, noinline block: PropertyBuilder.() -> Unit = {}): PropertyBuilder {
		val className = T::class.asClassName()
		return property(name, className, block)
	}

	fun property(name: String, type: ClassName, block: PropertyBuilder.() -> Unit = {}): PropertyBuilder {
		return if (properties.containsKey(name) && name != "path") {
			// while there's a suffix, add +1 to the suffix
			var newName = name
			var i = 1
			while (properties.containsKey(newName)) {
				newName = "$name$i"
				i++
			}
			properties[newName] = PropertyBuilder(newName, type).apply(block)
			properties[newName]!!
		} else {
			properties[name] = PropertyBuilder(name, type).apply(block)
			properties[name]!!
		}
	}


	fun function(name: String, block: FunSpec.Builder.() -> Unit): FunSpec.Builder {
		val builder = FunSpec.builder(name).apply(block)
		if (functions.containsKey(name)) {
			// while there's a suffix, add +1 to the suffix
			var newName = name
			var i = 1
			while (functions.containsKey(newName)) {
				newName = "$name$i"
				i++
			}
			if (functions[name]!!.parameters == builder.parameters) {
				functions[newName] = FunSpec.builder(newName).apply(block)
			} else {
				// if the parameters are different, the signature is different, no need to add a suffix
				// But use it for the map key (to avoid collision)
				functions[newName] = builder
			}
		} else {
			functions[name] = builder
		}
		return functions[name]!!
	}

	fun classBuilder(name: String, block: TypeBuilder.() -> Unit = {}): TypeBuilder {
		typeSpecs[name] = typeSpecs.getOrPut(name) {
            TypeBuilder(name, TypeSpec.Kind.CLASS)
        }.apply(block)
		return typeSpecs[name]!!
	}

	fun interfaceBuilder(name: String, block: TypeBuilder.() -> Unit = {}): TypeBuilder {
		typeSpecs[name] = typeSpecs.getOrPut(name) {
            TypeBuilder(name, TypeSpec.Kind.INTERFACE)
        }.apply(block)
		return typeSpecs[name]!!
	}


	fun objectBuilder(name: String, block: TypeBuilder.() -> Unit = {}): TypeBuilder {
		typeSpecs[name] = typeSpecs.getOrPut(name) {
            TypeBuilder(name, TypeSpec.Kind.OBJECT)
        }.apply(block)
		return typeSpecs[name]!!
	}

	companion object {
		fun classBuilder(name: String, block: TypeBuilder.() -> Unit = {}): TypeBuilder {
			return TypeBuilder(name, TypeSpec.Kind.CLASS).apply(block)
		}

		fun interfaceBuilder(name: String, block: TypeBuilder.() -> Unit = {}): TypeBuilder {
			return TypeBuilder(name, TypeSpec.Kind.INTERFACE).apply(block)
		}

		fun objectBuilder(name: String, block: TypeBuilder.() -> Unit = {}): TypeBuilder {
			return TypeBuilder(name, TypeSpec.Kind.OBJECT).apply(block)
		}

	}
}

fun FunSpec.Builder.addDocs(vararg docs: String): FunSpec.Builder {
	return addKdoc("%L", docs.joinToString("\n"))
}
