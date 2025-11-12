package io.github.e_psi_lon.kore.bindings.generation.poet

import com.squareup.kotlinpoet.*

internal class FileBuilder(
	packageName: String,
	fileName: String
) {
	private val builder = FileSpec.builder(packageName, fileName)
	// Contains all builders to attach to the file
	private val typeSpecs = mutableMapOf<String, TypeBuilder>()
	val propertySpecs = mutableMapOf<String, PropertySpec.Builder>()
	private val funSpecs = mutableMapOf<String, FunSpec.Builder>()

	fun addImport(import: ClassName, vararg names: String) = if (names.isEmpty()) {
		builder.addImport(import.packageName, import.simpleName)
	} else {
		builder.addImport(import.packageName, *names)
	}

	inline fun <reified T : Annotation> addAnnotation(noinline block: AnnotationSpec.Builder.() -> Unit = {}) =
		builder.addAnnotation(AnnotationSpec.builder(T::class).apply(block).build())

	fun file(block: FileSpec.Builder.() -> Unit) = builder.apply(block)

	fun classBuilder(name: String, block: TypeBuilder.() -> Unit) {
		typeSpecs[name] = typeSpecs.getOrPut(name) {
			TypeBuilder.classBuilder(name)
		}.apply(block)
	}


	fun interfaceBuilder(name: String, block: TypeBuilder.() -> Unit) {
		typeSpecs[name] = typeSpecs.getOrPut(name) {
			TypeBuilder.interfaceBuilder(name)
		}.apply(block)
	}

	fun objectBuilder(name: String, block: TypeBuilder.() -> Unit) {
		typeSpecs[name] = typeSpecs.getOrPut(name) {
			TypeBuilder.objectBuilder(name)
		}.apply(block)
	}

	fun property(name: String, type: ClassName, block: PropertySpec.Builder.() -> Unit) {
		propertySpecs[name] = propertySpecs.getOrPut(name) {
			PropertySpec.builder(name, type)
        }.apply(block)
	}

	inline fun <reified T : Any> property(name: String, noinline block: PropertySpec.Builder.() -> Unit) {
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
				this.builder.addType(it)
			}
		}
		propertySpecs.forEach { (_, builder) ->
			builder.build().let {
				this.builder.addProperty(it)
			}
		}
		// Add all functions to the file
		funSpecs.forEach { (_, builder) ->
			builder.build().let {
				this.builder.addFunction(it)
			}
		}
		// Add the package name to the file
		return builder.build()
	}
}

internal fun fileSpec(packageName: String, fileName: String, block: FileBuilder.() -> Unit): FileSpec {
	return FileBuilder(packageName, fileName).apply(block).build()
}