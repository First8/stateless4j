package com.github.oxo42.stateless4j.delegates;

import com.github.oxo42.stateless4j.StateMachineContext;

/**
 * Represents a function that produces a result.
 *
 * @param <R> Result type
 */
public interface Func<R,S,T> {

    /**
     * Applies this function
     *
     * @return Result
     */
    R call(StateMachineContext<S,T> context, Object ... args);
}
