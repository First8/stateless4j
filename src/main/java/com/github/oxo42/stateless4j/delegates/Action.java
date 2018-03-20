package com.github.oxo42.stateless4j.delegates;

import com.github.oxo42.stateless4j.StateMachineContext;
import com.github.oxo42.stateless4j.transitions.Transition;

/**
 * Represents an operation that accepts no input arguments and returns no result.
 */
public interface Action<S,T> {

    /**
     * Performs this operation
     */
    void doIt(StateMachineContext<S,T> context, Transition<S,T> transition, Object ... args);
}
