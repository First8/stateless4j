package com.github.oxo42.stateless4j;

import com.github.oxo42.stateless4j.delegates.Action1;
import com.github.oxo42.stateless4j.delegates.Func;

/**
 * Models behaviour as transitions between a finite set of states. Has a reference to a stateMachineContext, which allows for the storing of context
 * information for this state machine.
 *
 * @param <S> The type used to represent the states
 * @param <T> The type used to represent the triggers that cause state transitions
 */
public class ContextedStateMachine<S, T> extends StateMachine<S, T> {

    private final StateMachineContext<S, T> stateMachineContext;

    /**
     * Construct a state machine.
     *
     * @param initialState The initial state
     */
    public ContextedStateMachine(S initialState) {
        this(initialState, new DefaultStateMachineContext<>());
    }

    /**
     * Construct a state machine with a {@link StateMachineContext}.
     *
     * @param initialState The initial state
     * @param context      State machine context
     */
    public ContextedStateMachine(S initialState, StateMachineContext<S, T> context) {
        super(initialState);
        this.stateMachineContext = context;
        this.stateMachineContext.setContextedStateMachine(this);
    }

    /**
     * Construct a state machine with a {@link StateMachineConfig}.
     *
     * @param initialState The initial state
     * @param config       State machine configuration
     */
    public ContextedStateMachine(S initialState, StateMachineConfig<S, T> config) {
        this(initialState, config, new DefaultStateMachineContext<>());
    }

    /**
     * Construct a state machine with a with a {@link StateMachineConfig} and a {@link StateMachineContext}.
     *
     * @param initialState The initial state
     * @param config       State machine configuration
     * @param context      State machine context
     */
    public ContextedStateMachine(S initialState, StateMachineConfig<S, T> config, StateMachineContext<S, T> context) {
        super(initialState, config);
        this.stateMachineContext = context;
        this.stateMachineContext.setContextedStateMachine(this);
    }

    /**
     * Construct a state machine with external state storage and {@link StateConfiguration}.
     *
     * @param initialState  The initial state
     * @param stateAccessor State accessor
     * @param stateMutator  State mutator
     * @param config        State machine configuration
     */
    public ContextedStateMachine(S initialState, Func<S> stateAccessor, Action1<S> stateMutator, StateMachineConfig<S, T> config) {
        this(initialState, stateAccessor, stateMutator, config, new DefaultStateMachineContext<>());
    }

    /**
     * Construct a state machine with external state storage, {@link StateConfiguration} and a {@link StateMachineContext}.
     *
     * @param initialState  The initial state
     * @param stateAccessor State accessor
     * @param stateMutator  State mutator
     * @param config        State machine configuration
     * @param context       State machine context
     */
    public ContextedStateMachine(S initialState, Func<S> stateAccessor, Action1<S> stateMutator, StateMachineConfig<S, T> config, StateMachineContext<S, T> context) {
        super(initialState, stateAccessor, stateMutator, config);
        this.stateMachineContext = context;
        this.stateMachineContext.setContextedStateMachine(this);
    }

    /**
     * Get the {@link StateMachineContext} of this contexted state machine. This context allows for the storing of context information for this state machine.
     *
     * @return the context of this state machine
     */
    public StateMachineContext getStateMachineContext() {
        return stateMachineContext;
    }
}
