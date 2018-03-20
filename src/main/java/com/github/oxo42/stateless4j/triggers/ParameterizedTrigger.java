package com.github.oxo42.stateless4j.triggers;

import com.github.oxo42.stateless4j.conversion.ParameterConversion;

public class ParameterizedTrigger<TState, TTrigger> {

    private final TTrigger underlyingTrigger;
    private final Class<?>[] argumentTypes;

    /**
     * Create a configured trigger
     *
     * @param underlyingTrigger Trigger represented by this trigger configuration
     * @param argumentTypes     The argument types expected by the trigger
     */
    public ParameterizedTrigger(final TTrigger underlyingTrigger, final Class<?>... argumentTypes) {
        assert argumentTypes != null : "argumentTypes is null";

        this.underlyingTrigger = underlyingTrigger;
        this.argumentTypes = argumentTypes;
    }

    /**
     * Gets the underlying trigger value that has been configured
     *
     * @return Gets the underlying trigger value that has been configured
     */
    public TTrigger getTrigger() {
        return underlyingTrigger;
    }

    /**
     * Ensure that the supplied arguments are compatible with those configured for this trigger
     *
     * @param args Args
     */
    public void validateParameters(Object[] args) {
        if( args != null ) {
            ParameterConversion.validate(args, argumentTypes);
        } else {
            if( argumentTypes.length > 0 ) {
                throw new IllegalStateException(
                        String.format("Not enough parameters have been supplied. Expecting %s but got 0.", argumentTypes.length));
            }
        }
    }
}
