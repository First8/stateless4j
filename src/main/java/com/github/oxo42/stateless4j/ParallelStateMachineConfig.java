package com.github.oxo42.stateless4j;

public class ParallelStateMachineConfig<TState,TTrigger> extends StateMachineConfig<TState,TTrigger> {

	private TState initialState;
	private StateMachineConfig<TState,TTrigger> parentConfiguration;

	public ParallelStateMachineConfig(TState initialState, StateMachineConfig<TState,TTrigger> parentConfiguration) {
		this.initialState = initialState;
		this.parentConfiguration = parentConfiguration;
	}

	public TState getInitialState() {
		return initialState;
	}

	public StateMachineConfig<TState,TTrigger> getParentConfiguration() {
		return parentConfiguration;
	}
}
