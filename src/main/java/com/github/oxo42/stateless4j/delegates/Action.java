package com.github.oxo42.stateless4j.delegates;

import com.github.oxo42.stateless4j.StateMachineContext;

/**
 * Represents an operation that accepts no input arguments and returns no result.
 */
public interface Action<S extends StateMachineContext> {

    /**
     * Performs this operation
     */
    void doIt(S context);
}
