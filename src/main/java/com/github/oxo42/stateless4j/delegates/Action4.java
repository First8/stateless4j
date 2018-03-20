package com.github.oxo42.stateless4j.delegates;

import com.github.oxo42.stateless4j.StateMachineContext;

/**
 * Represents an operation that accepts an input and returns no result
 *
 * @param <T>  The type of the input to the operation
 * @param <T1> The type of the input to the operation
 * @param <T2> The type of the input to the operation
 * @param <T3> The type of the input to the operation
 */
public interface Action4<S,T, U, U1, U2, U3> {

    /**
     * Performs this operation on the given input
     *
     * @param arg1 Input argument
     * @param arg2 Input argument
     * @param arg3 Input argument
     * @param arg4 Input argument
     */
    void doIt(StateMachineContext<S,T> context, U arg1, U1 arg2, U2 arg3, U3 arg4);
}
