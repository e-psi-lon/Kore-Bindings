package io.github.e_psi_lon.kore.bindings.utils

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.configuration
import kotlin.io.path.Path

data class TestDataPack(internal val dp: DataPack) {
    private val calledAfterGeneration = mutableListOf<DataPack.() -> Unit>()

    init {
        dp.path = Path("out")
    }

    fun callAfterGeneration(block: DataPack.() -> Unit) {
        calledAfterGeneration += block
    }

    fun generate() {
        dp.generate()
        calledAfterGeneration.forEach { it(dp) }
    }

    fun generateZip() {
        dp.generateZip()
        calledAfterGeneration.forEach { it(dp) }
    }
}

internal fun testDataPack(name: String, block: DataPack.() -> Unit): TestDataPack {
    val testDataPack = TestDataPack(DataPack(name))
    testDataPack.dp.apply(block)
    return testDataPack
}

fun DataPack.pretty() = configuration {
    prettyPrint = true
    prettyPrintIndent = "\t"
}