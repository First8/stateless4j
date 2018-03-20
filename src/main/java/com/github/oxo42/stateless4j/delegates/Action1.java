package com.github.oxo42.stateless4j.delegates;

import com.github.oxo42.stateless4j.StateMachineContext;

/**
 * Represents an operation that accepts an input and returns no result
 *
 * @param <T> The type of the input to the operation
 */
public interface Action1<S,T,U> {

    /**
     * Performs this operation on the given input
     *
     * @param arg1 Input argument
     */
    void doIt(StateMachineContext<S,T> context, U arg1);
}
