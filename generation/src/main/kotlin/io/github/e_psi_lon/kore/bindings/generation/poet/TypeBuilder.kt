package io.github.e_psi_lon.kore.bindings.generation.poet

import com.squareup.kotlinpoet.*

class TypeBuilder(
	name: String,
	type: TypeSpec.Kind
) {
	private var builder = when (type) {
		TypeSpec.Kind.INTERFACE -> TypeSpec.interfaceBuilder(name)
		TypeSpec.Kind.CLASS -> TypeSpec.classBuilder(name)
		TypeSpec.Kind.OBJECT -> TypeSpec.objectBuilder(name)
		else -> throw IllegalArgumentException("Unknown type: $type")
	}
	val properties = mutableMapOf<String, PropertyBuilder>()
	val functions = mutableMapOf<String, FunSpec.Builder>()
	private val typeSpecs = mutableMapOf<String, TypeBuilder>()

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


	inline fun <reified T : Any> property(name: String, block: PropertyBuilder.() -> Unit): PropertyBuilder {
		properties[name] = if (properties.containsKey(name)) {
			properties[name]!!.apply(block)
		} else {
			PropertyBuilder(name, T::class.asClassName()).apply(block)
		}
		return properties[name]!!
	}

	fun property(name: String, type: ClassName, block: PropertyBuilder.() -> Unit): PropertyBuilder {
		if (properties.containsKey(name)) {
			// while there's a suffix, add +1 to the suffix
			var newName = name
			var i = 1
			while (properties.containsKey(newName)) {
				newName = "$name$i"
				i++
			}
			properties[newName] = PropertyBuilder(newName, type).apply(block)
			return properties[newName]!!
		} else {
			properties[name] = PropertyBuilder(name, type).apply(block)
			return properties[name]!!
		}
	}

	fun function(name: String, block: FunSpec.Builder.() -> Unit): FunSpec.Builder {
		if (functions.containsKey(name)) {
			// while there's a suffix, add +1 to the suffix
			var newName = name
			var i = 1
			while (functions.containsKey(newName)) {
				newName = "$name$i"
				i++
			}
			functions[newName] = FunSpec.builder(newName).apply(block)
		} else {
			functions[name] = FunSpec.builder(name).apply(block)
		}
		return functions[name]!!
	}

	fun classBuilder(name: String, block: TypeBuilder.() -> Unit): TypeBuilder {
		typeSpecs[name] = if (typeSpecs.containsKey(name)) {
			typeSpecs[name]!!.apply(block)
		} else {
			TypeBuilder(name, TypeSpec.Kind.INTERFACE).apply(block)
		}
		return typeSpecs[name]!!
	}

	fun interfaceBuilder(name: String, block: TypeBuilder.() -> Unit): TypeBuilder {
		typeSpecs[name] = if (typeSpecs.containsKey(name)) {
			typeSpecs[name]!!.apply(block)
		} else {
			TypeBuilder(name, TypeSpec.Kind.INTERFACE).apply(block)
		}
		return typeSpecs[name]!!
	}


	fun objectBuilder(name: String, block: TypeBuilder.() -> Unit): TypeBuilder {
		val contains = typeSpecs.containsKey(name)
		typeSpecs[name] = if (typeSpecs.containsKey(name)) {
			println("An object with the name $name already exists. Extending it.")
			println("It had the following properties: ${typeSpecs[name]!!.properties.keys}")
			println("It had the following functions: ${typeSpecs[name]!!.functions.keys}")
			println("It had the following types: ${typeSpecs[name]!!.typeSpecs.keys}")
			typeSpecs[name]!!.apply(block)
		} else {
			TypeBuilder(name, TypeSpec.Kind.OBJECT).apply(block)
		}
		if (contains) {
			println("And now it has the following properties: ${typeSpecs[name]!!.properties.keys}")
			println("And now it has the following functions: ${typeSpecs[name]!!.functions.keys}")
			println("And now it has the following types: ${typeSpecs[name]!!.typeSpecs.keys}")
		}
		return typeSpecs[name]!!
	}

	companion object {
		fun classBuilder(name: String, block: TypeBuilder.() -> Unit): TypeBuilder {
			return TypeBuilder(name, TypeSpec.Kind.CLASS).apply(block)
		}

		fun interfaceBuilder(name: String, block: TypeBuilder.() -> Unit): TypeBuilder {
			return TypeBuilder(name, TypeSpec.Kind.INTERFACE).apply(block)
		}

		fun objectBuilder(name: String, block: TypeBuilder.() -> Unit): TypeBuilder {
			return TypeBuilder(name, TypeSpec.Kind.OBJECT).apply(block)
		}

	}
}