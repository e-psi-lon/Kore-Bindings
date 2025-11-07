package io.github.e_psi_lon.kore.bindings.generation.components


/**
 * Represents a source of parameter values for component generation.
 *
 * This sealed interface defines the possible sources from which parameter values
 * can be derived when generating binding code for datapack components. It's used
 * by the code generation system to determine what value to pass for each parameter
 * in the generated getter methods.
 */
sealed interface ParameterValueSource {
    /**
     * Indicates that the parameter value should come from the component's name.
     *
     * When used, the generated code will pass the component's fileName as the
     * parameter value (e.g., "my_advancement" for an advancement component).
     */
    data object Name : ParameterValueSource

    /**
     * Indicates that the parameter value should come from the component's namespace.
     *
     * When used, the generated code will pass the namespace name as the parameter
     * value (e.g., "minecraft" or "smithed.crafter").
     */
    data object Namespace : ParameterValueSource

    /**
     * Indicates that the parameter should use an explicit default value.
     *
     * When used, the generated code will pass the provided value directly as a
     * literal in the generated binding code.
     *
     * @param T The type of the default value.
     * @property value The literal value to use for this parameter in generated code.
     */
    data class Default<T : Any>(val value: T) : ParameterValueSource


    data object SelfSafeReference : ParameterValueSource
}
