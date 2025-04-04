package io.github.e_psi_lon.kore.bindings.generation.poet

import com.squareup.kotlinpoet.*

class TypeBuilder(
	private val name: String,
	private val type: String
) {
	val properties = mutableMapOf<String, PropertyBuilder>()
	val functions = mutableMapOf<String, FunSpec.Builder>()
	private val typeSpecs = mutableMapOf<String, TypeBuilder>()

	fun build(): TypeSpec {
		return when (type) {
			"interface" -> TypeSpec.interfaceBuilder(name)
			"class" -> TypeSpec.classBuilder(name)
			"enum" -> TypeSpec.enumBuilder(name)
			"object" -> TypeSpec.objectBuilder(name)
			else -> throw IllegalArgumentException("Unknown type: $type")
		}.apply {
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

	inline fun <reified T : Any> property(name: String, block: PropertyBuilder.() -> Unit) {
		properties[name] = if (properties.containsKey(name)) {
			properties[name]!!.apply(block)
		} else {
			PropertyBuilder(name, T::class.asClassName()).apply(block)
		}
	}

	fun property(name: String, type: ClassName, block: PropertyBuilder.() -> Unit) {
		if (properties.containsKey(name)) {
			// while there's a suffix, add +1 to the suffix
			var newName = name
			var i = 1
			while (properties.containsKey(newName)) {
				newName = "$name$i"
				i++
			}
			properties[newName] = PropertyBuilder(newName, type).apply(block)
		} else {
			properties[name] = PropertyBuilder(name, type).apply(block)
		}
	}

	fun function(name: String, block: FunSpec.Builder.() -> Unit) {
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
	}

	fun classBuilder(name: String, block: TypeBuilder.() -> Unit) {
		typeSpecs[name] = if (typeSpecs.containsKey(name)) {
			typeSpecs[name]!!.apply(block)
		} else {
			TypeBuilder(name, "class").apply(block)
		}
	}

	fun interfaceBuilder(name: String, block: TypeBuilder.() -> Unit) {
		typeSpecs[name] = if (typeSpecs.containsKey(name)) {
			typeSpecs[name]!!.apply(block)
		} else {
			TypeBuilder(name, "interface").apply(block)
		}
	}

	fun enumBuilder(name: String, block: TypeBuilder.() -> Unit) {
		typeSpecs[name] = if (typeSpecs.containsKey(name)) {
			typeSpecs[name]!!.apply(block)
		} else {
			TypeBuilder(name, "enum").apply(block)
		}
	}

	fun objectBuilder(name: String, block: TypeBuilder.() -> Unit) {
		val contains = typeSpecs.containsKey(name)
		typeSpecs[name] = if (typeSpecs.containsKey(name)) {
			println("An object with the name $name already exists. Extending it.")
			println("It had the following properties: ${typeSpecs[name]!!.properties.keys}")
			println("It had the following functions: ${typeSpecs[name]!!.functions.keys}")
			println("It had the following types: ${typeSpecs[name]!!.typeSpecs.keys}")
			typeSpecs[name]!!.apply(block)
		} else {
			TypeBuilder(name, "object").apply(block)
		}
		if (contains) {
			println("And now it has the following properties: ${typeSpecs[name]!!.properties.keys}")
			println("And now it has the following functions: ${typeSpecs[name]!!.functions.keys}")
			println("And now it has the following types: ${typeSpecs[name]!!.typeSpecs.keys}")
		}
	}

	companion object {
		fun classBuilder(name: String, block: TypeBuilder.() -> Unit): TypeBuilder {
			return TypeBuilder(name, "class").apply(block)
		}

		fun interfaceBuilder(name: String, block: TypeBuilder.() -> Unit): TypeBuilder {
			return TypeBuilder(name, "interface").apply(block)
		}

		fun enumBuilder(name: String, block: TypeBuilder.() -> Unit): TypeBuilder {
			return TypeBuilder(name, "enum").apply(block)
		}

		fun objectBuilder(name: String, block: TypeBuilder.() -> Unit): TypeBuilder {
			return TypeBuilder(name, "object").apply(block)
		}

	}
}