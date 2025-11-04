package io.github.e_psi_lon.kore.bindings.generation

import io.github.ayfri.kore.utils.camelCase
import io.github.ayfri.kore.utils.pascalCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Sanitize a string for use as a Kotlin identifier and convert it to camelCase.
 * Replaces dots with underscores after conversion.
 *
 * @return The sanitized string in camelCase
 */
fun String.sanitizeCamel() = camelCase().replace('.', '_')

/**
 * Sanitize a string for use as a Kotlin identifier and convert it to PascalCase.
 * Replaces dots with underscores after conversion.
 *
 * @return The sanitized string in PascalCase
 */
fun String.sanitizePascal() = pascalCase().replace('.', '_')

/**
 * Checks if the given name is a valid Kotlin identifier.
 * Returns false if it starts with a number or contains invalid characters.
 */
fun String.isValidKotlinIdentifier(): Boolean {
    if (isEmpty()) return false
    if (this[0].isDigit()) return false
    return all { it.isLetterOrDigit() }
}

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
    map {
        async(dispatcher) {
            block(it)
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
    map {
        async(dispatcher) {
            block(it)
        }
    }.awaitAll()
}
