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

    /**
     * Determine if the state machine state currently contains the specified state
     *
     * @param state The state to test for
     * @return True if the state is found, else false
     */
	public boolean isInState(S state) {
        return this.state.equals(state) || this.subStates.stream().anyMatch(s -> s.isInState(state));
    }

	@Override
	public String toString() {
		return "StateMachineState{" +
				"state=" + state +
				", subStates=" + subStates +
				'}';
	}
}
