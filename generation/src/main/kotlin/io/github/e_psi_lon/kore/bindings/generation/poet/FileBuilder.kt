package io.github.e_psi_lon.kore.bindings.generation.poet

import com.squareup.kotlinpoet.*

class FileBuilder(
	packageName: String,
	fileName: String
) {
	private var builder = FileSpec.builder(packageName, fileName)
	// Contains all builders to attach to the file
	private val typeSpecs = mutableMapOf<String, TypeBuilder>()
	val propertySpecs = mutableMapOf<String, PropertyBuilder>()
	private val funSpecs = mutableMapOf<String, FunSpec.Builder>()

	fun addImport(import: ClassName, vararg names: String) {
		builder = builder.addImport(import, *names)
	}

	fun file(block: FileSpec.Builder.() -> Unit) {
		builder = this.builder.apply(block)
	}

	fun classBuilder(name: String, block: TypeBuilder.() -> Unit) {
		typeSpecs[name] = if (typeSpecs.containsKey(name)) {
			typeSpecs[name]!!.apply(block)
		} else {
			TypeBuilder.classBuilder(name, block)
		}
	}


	fun interfaceBuilder(name: String, block: TypeBuilder.() -> Unit) {
		typeSpecs[name] = if (typeSpecs.containsKey(name)) {
			typeSpecs[name]!!.apply(block)
		} else {
			TypeBuilder.interfaceBuilder(name, block)
		}
	}

	fun objectBuilder(name: String, block: TypeBuilder.() -> Unit) {
		typeSpecs[name] = if (typeSpecs.containsKey(name)) {
			typeSpecs[name]!!.apply(block)
		} else {
			TypeBuilder.objectBuilder(name, block)
		}
	}

	fun property(name: String, type: ClassName, block: PropertyBuilder.() -> Unit) {
		propertySpecs[name] = if (propertySpecs.containsKey(name)) {
			propertySpecs[name]!!.apply(block)
		} else {
			PropertyBuilder(name, type).apply(block)
		}
	}

	inline fun <reified T : Any> property(name: String, noinline block: PropertyBuilder.() -> Unit) {
		val className = T::class.asClassName()
		property(name, className, block)
	}

	fun function(name: String, block: FunSpec.Builder.() -> Unit): FunSpec.Builder {
		val builder = FunSpec.builder(name).apply(block)
		if (funSpecs.containsKey(name)) {
			// while there's a suffix, add +1 to the suffix
			var newName = name
			var i = 1
			while (funSpecs.containsKey(newName)) {
				newName = "$name$i"
				i++
			}
			if (funSpecs[name]!!.parameters == builder.parameters) {
				funSpecs[newName] = FunSpec.builder(newName).apply(block)
			} else {
				// if the parameters are different, the signature is different, no need to add a suffix
				// But use it for the map key (to avoid collision)
				funSpecs[newName] = builder
			}
		} else {
			funSpecs[name] = builder
		}
		return funSpecs[name]!!
	}


	fun build(): FileSpec {
		// Add all builders to the file
		typeSpecs.forEach { (_, builder) ->
			builder.build().let {
				this.builder = this.builder.addType(it)
			}
		}
		propertySpecs.forEach { (_, builder) ->
			builder.build().let {
				this.builder = this.builder.addProperty(it)
			}
		}
		// Add all functions to the file
		funSpecs.forEach { (_, builder) ->
			builder.build().let {
				this.builder = this.builder.addFunction(it)
			}
		}
		// Add the package name to the file
		return this.builder.build()
	}
}

fun fileSpec(packageName: String, fileName: String, block: FileBuilder.() -> Unit): FileSpec {
	return FileBuilder(packageName, fileName).apply(block).build()
}