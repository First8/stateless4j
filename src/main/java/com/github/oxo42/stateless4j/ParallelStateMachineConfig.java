package com.github.oxo42.stateless4j;

public class ParallelStateMachineConfig<TState,TTrigger> extends StateMachineConfig<TState,TTrigger> {

	private TState initialState;

	public ParallelStateMachineConfig(TState initialState) {
		this.initialState = initialState;
	}

	public TState getInitialState() {
		return initialState;
	}
}
