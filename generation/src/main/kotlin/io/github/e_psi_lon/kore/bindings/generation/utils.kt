package io.github.e_psi_lon.kore.bindings.generation

import io.github.ayfri.kore.utils.pascalCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


/**
 * Sanitize a string for use as a Kotlin identifier and convert it to camelCase.
 *
 * @return The sanitized string in camelCase
 */
fun String.sanitizeCamel() = sanitizePascal().replaceFirstChar { if (!it.isLowerCase()) it.lowercase() else it.toString() }

/**
 * Sanitize a string for use as a Kotlin identifier and convert it to PascalCase.
 *
 * @return The sanitized string in PascalCase
 */
fun String.sanitizePascal() = pascalCase()
    .replace('-', '_')
    .replace(".", "_")

/**
 * Maps each element of the collection to a result using the given suspend block,
 * executing all transformations in parallel on the specified dispatcher.
 *
 * @param dispatcher The coroutine dispatcher to use for parallel execution
 * @param block The suspend transformation function to apply to each element
 * @return A list of results in the same order as the input collection
 */
suspend fun <T, R> Iterable<T>.mapAsync(
    dispatcher: CoroutineDispatcher,
    block: suspend (T) -> R
): List<R> = coroutineScope {
    map { element ->
        async(dispatcher) {
            block(element)
        }
    }.awaitAll()
}

/**
 * Maps each element of the array to a result using the given suspend block,
 * executing all transformations in parallel on the specified dispatcher.
 *
 * @param dispatcher The coroutine dispatcher to use for parallel execution
 * @param block The suspend transformation function to apply to each element
 * @return A list of results in the same order as the input array
 */
suspend fun <T, R> Array<T>.mapAsync(
    dispatcher: CoroutineDispatcher,
    block: suspend (T) -> R
): List<R> = coroutineScope {
    map { element ->
        async(dispatcher) {
            block(element)
        }
    }.awaitAll()
}
