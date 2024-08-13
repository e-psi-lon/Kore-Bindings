package io.github.e_psi_lon.kore.bindings.assertions

fun printStackTraceAndDiff(difference: String, stackIndex: Int) {
    val stack = Thread.currentThread().stackTrace
    with(stack[stackIndex]) {
        System.err.println("\nat $className.$methodName($fileName:$lineNumber)")
    }
    System.err.println(difference)
}