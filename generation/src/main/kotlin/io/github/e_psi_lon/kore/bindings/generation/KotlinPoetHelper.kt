package io.github.e_psi_lon.kore.bindings.generation

import com.squareup.kotlinpoet.*

fun fileSpec(packageName: String, fileName: String, block: FileSpec.Builder.() -> Unit): FileSpec {
	return FileSpec.builder(packageName, fileName).apply(block).build()
}

fun FileSpec.Builder.classBuilder(name: String, block: TypeSpec.Builder.() -> Unit) {
	addType(TypeSpec.classBuilder(name).apply(block).build())
}

fun TypeSpec.Builder.classBuilder(name: String, block: TypeSpec.Builder.() -> Unit) {
	addType(TypeSpec.classBuilder(name).apply(block).build())
}

fun FileSpec.Builder.enumBuilder(name: String, block: TypeSpec.Builder.() -> Unit) {
	addType(TypeSpec.enumBuilder(name).apply(block).build())
}

fun TypeSpec.Builder.enumBuilder(name: String, block: TypeSpec.Builder.() -> Unit) {
	addType(TypeSpec.enumBuilder(name).apply(block).build())
}

fun FileSpec.Builder.interfaceBuilder(name: String, block: TypeSpec.Builder.() -> Unit) {
	addType(TypeSpec.interfaceBuilder(name).apply(block).build())
}

fun TypeSpec.Builder.interfaceBuilder(name: String, block: TypeSpec.Builder.() -> Unit) {
	addType(TypeSpec.interfaceBuilder(name).apply(block).build())
}

fun FileSpec.Builder.objectBuilder(name: String, block: TypeSpec.Builder.() -> Unit) {
	addType(TypeSpec.objectBuilder(name).apply(block).build())
}

fun FileSpec.Builder.property(name: String, type: ClassName, block: PropertySpec.Builder.() -> Unit) {
	addProperty(PropertySpec.builder(name, type).apply(block).build())
}

fun PropertySpec.Builder.getter(block: FunSpec.Builder.() -> Unit) {
	getter(FunSpec.getterBuilder().apply(block).build())
}

fun TypeSpec.Builder.objectBuilder(name: String, block: TypeSpec.Builder.() -> Unit) {
	addType(TypeSpec.objectBuilder(name).apply(block).build())
}


inline fun <reified T : Any>TypeSpec.Builder.property(name: String, block: PropertySpec.Builder.() -> Unit) {
	addProperty(PropertySpec.builder(name, T::class.asClassName()).apply(block).build())
}

fun TypeSpec.Builder.property(name: String, type: ClassName, block: PropertySpec.Builder.() -> Unit) {
	addProperty(PropertySpec.builder(name, type).apply(block).build())
}

fun TypeSpec.Builder.function(name: String, block: FunSpec.Builder.() -> Unit) {
	addFunction(FunSpec.builder(name).apply(block).build())
}