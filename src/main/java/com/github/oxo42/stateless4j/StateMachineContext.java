package com.github.oxo42.stateless4j;

import java.util.Map;
import java.util.stream.Stream;

/**
 * This interface describes a state machine context, which can be used by a {@link StateMachine} to store context information.
 *
 * @param <S> The type used to represent the states of the contexted state machine that this context belongs to
 * @param <T> The type used to represent the triggers that cause state transitions of the contexted state machine that this context belongs to
 */
public interface StateMachineContext<S, T> {

    /**
     * Set the {@link StateMachine} that this context belongs to.
     *
     * @param stateMachine The state machine to set
     */
    void setStateMachine(StateMachine<S, T> stateMachine);

    /**
     * Get a map of context attributes. Used to store context information.
     *
     * @return A map of context attributes
     */
    Map<String, Object> getAttributes();

    /**
     * Get a specific attribute from the context attribute map, using the specified key.
     *
     * @param key the key of the specified attribute
     * @return the value, if any, of the specified key
     */
    Object getAttribute(String key);

    /**
     * Get a specific attribute from the context attribute map, using the specified key. If
     * the key has no value it is initialized with the given initial value.
     *
     * @param key the key of the specified attribute
     * @param initialValue the initial value if key is absent
     * @return the value of the specified key, null if initialValue is null and key was absent.
     */
    Object getAttribute(String key, Object initialValue);

    /**
     * Set an attribute on the context attribute map, using the specified key.
     *
     * @param key the name of the key to use for the attribute
     * @param value the value of the attribute to add
     * @return the value of the attribute to add, if it was successfully added
     */
    Object setAttribute(String key, Object value);

    /**
     * Get a stream of the context attributes.
     *
     * @return A stream of the map of context attributes
     */
    Stream<Map.Entry<String,Object>> attributes();

    /**
     * Get the {@link StateMachine} that this context belongs to.
     *
     * @return the contexted state machine that this context belongs to
     */
    StateMachine<S, T> getTopLevelStateMachine();

}
