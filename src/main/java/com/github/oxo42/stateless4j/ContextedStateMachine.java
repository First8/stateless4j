package com.github.oxo42.stateless4j;

import com.github.oxo42.stateless4j.delegates.Action1;
import com.github.oxo42.stateless4j.delegates.Func;

public class ContextedStateMachine<S,T> extends StateMachine<S,T>{

	private final StateMachineContext stateMachineContext = new StateMachineContext();

	public ContextedStateMachine(S initialState) {
		super(initialState);
	}

	public ContextedStateMachine(S initialState, StateMachineConfig<S, T> config) {
		super(initialState, config);
	}

	public ContextedStateMachine(S initialState, Func<S> stateAccessor, Action1<S> stateMutator, StateMachineConfig<S, T> config) {
		super(initialState, stateAccessor, stateMutator, config);
	}

	public StateMachineContext getStateMachineContext() {
		return stateMachineContext;
	}
}
