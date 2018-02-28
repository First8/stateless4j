package com.github.oxo42.stateless4j;

import com.github.oxo42.stateless4j.delegates.Action1;
import com.github.oxo42.stateless4j.delegates.Func;

public class ContextedStateMachine<S,T> extends StateMachine<S,T>{

	private final StateMachineContext<S, T> stateMachineContext;

	public ContextedStateMachine(S initialState) {
		super(initialState);
        this.stateMachineContext = new StateMachineContext<>(this);
    }

	public ContextedStateMachine(S initialState, StateMachineConfig<S, T> config) {
		super(initialState, config);
		this.stateMachineContext = new StateMachineContext<>(this);
	}

	public ContextedStateMachine(S initialState, Func<S> stateAccessor, Action1<S> stateMutator, StateMachineConfig<S, T> config) {
		super(initialState, stateAccessor, stateMutator, config);
        this.stateMachineContext = new StateMachineContext<>(this);
    }

	public StateMachineContext getStateMachineContext() {
		return stateMachineContext;
	}
}
