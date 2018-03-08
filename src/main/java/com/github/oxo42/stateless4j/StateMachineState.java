package com.github.oxo42.stateless4j;

import java.util.ArrayList;
import java.util.List;

public class StateMachineState<S> {

	private S state;
	private List<StateMachineState<S>> subStates;

	public StateMachineState(S state) {
		this(state,new ArrayList<>());
	}

	public StateMachineState(S state, List<StateMachineState<S>> subStates) {
		this.state = state;
		this.subStates = subStates;
	}

	public S getState() {
		return state;
	}

	public List<StateMachineState<S>> getSubStates() {
		return subStates;
	}

	@Override
	public String toString() {
		return "StateMachineState{" +
				"state=" + state +
				", subStates=" + subStates +
				'}';
	}
}
