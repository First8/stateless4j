package com.github.oxo42.stateless4j;

import java.util.Map;
import java.util.stream.Stream;

/**
 * This interface describes a state machine context, which can be used by a {@link ContextedStateMachine} to store context information.
 *
 * @param <S> The type used to represent the states of the contexted state machine that this context belongs to
 * @param <T> The type used to represent the triggers that cause state transitions of the contexted state machine that this context belongs to
 */
public interface StateMachineContext<S, T> {

    /**
     * Set the {@link ContextedStateMachine} that this context belongs to.
     *
     * @param contextedStateMachine The contexted state machine to set
     */
    void setContextedStateMachine(ContextedStateMachine<S, T> contextedStateMachine);

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
     * Get the {@link ContextedStateMachine} that this context belongs to.
     *
     * @return the contexted state machine that this context belongs to
     */
    ContextedStateMachine<S, T> getContextedStateMachine();
}
