package com.github.oxo42.stateless4j;

import com.github.oxo42.stateless4j.delegates.Action1;
import com.github.oxo42.stateless4j.delegates.Action2;
import com.github.oxo42.stateless4j.delegates.Func;
import com.github.oxo42.stateless4j.transitions.Transition;
import com.github.oxo42.stateless4j.triggers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Models behaviour as transitions between a finite set of states
 *
 * @param <S> The type used to represent the states
 * @param <T> The type used to represent the triggers that cause state transitions
 */
public class StateMachine<S, T> {

	protected final StateMachineConfig<S, T> config;
	protected final Func<S> stateAccessor;
	protected final Action1<S> stateMutator;
	private final Map<S, List<StateMachine<S, T>>> parallelStateMachines = new HashMap<>();
	protected Action2<S, T> unhandledTriggerAction = (state, trigger) -> {
		throw new IllegalStateException(
				String.format(
						"No valid leaving transitions are permitted from state '%s' for trigger '%s'. Consider ignoring the trigger.",
						state, trigger)
		);
	};

	/**
	 * Construct a state machine
	 *
	 * @param initialState The initial state
	 */
	public StateMachine(final S initialState) {
		this(initialState, new StateMachineConfig<>());
	}

	/**
	 * Construct a state machine
	 *
	 * @param initialState The initial state
	 * @param config       State machine configuration
	 */
	public StateMachine(final S initialState, final StateMachineConfig<S, T> config) {
		this.config = config;
		final StateReference<S, T> reference = new StateReference<>();
		reference.setState(initialState);
		stateAccessor = reference::getState;
		stateMutator = reference::setState;

		if (config.isEntryActionOfInitialStateEnabled()) {
			Transition<S, T> initialTransition = new Transition<>(initialState, initialState, null);
			getCurrentRepresentation().enter(initialTransition);
		}
	}

	/**
	 * Construct a state machine with external state storage.
	 *
	 * @param initialState  The initial state
	 * @param stateAccessor State accessor
	 * @param stateMutator  State mutator
	 * @param config        State machine configuration
	 */
	public StateMachine(final S initialState, final Func<S> stateAccessor, final Action1<S> stateMutator, final StateMachineConfig<S, T> config) {
		this.config = config;
		this.stateAccessor = stateAccessor;
		this.stateMutator = stateMutator;
		stateMutator.doIt(initialState);
	}

	public StateConfiguration<S, T> configure(final S state) {
		return config.configure(state);
	}

	public StateMachineConfig<S, T> configuration() {
		return config;
	}

	/**
	 * The current state
	 *
	 * @return The current state
	 */
	public S getState() {
		return stateAccessor.call();
	}

	private void setState(S value) {
		stateMutator.doIt(value);
	}

	/**
	 * Retrieves the current state of the state machine including all parallel states active.
	 *
	 * @return StateMachineState the state of the state machine.
	 */
	public StateMachineState<S> getStateMachineState() {
		S currentState = getCurrentRepresentation().getUnderlyingState();
		if (getCurrentRepresentation().isParallelState()) {
			if (parallelStateMachines.containsKey(currentState)) {
				return new StateMachineState<>(currentState, parallelStateMachines.get(currentState).stream().map(s -> s.getStateMachineState()).collect(Collectors.toList()));
			} else {
				throw new IllegalStateException("State " + currentState + " is parallel, but no parallel states found.");
			}
		}
		return new StateMachineState<>(currentState);
	}

	/**
	 * The currently-permissible trigger values
	 *
	 * @return The currently-permissible trigger values
	 */
	public List<T> getPermittedTriggers() {
		return getCurrentRepresentation().getPermittedTriggers();
	}

	StateRepresentation<S, T> getCurrentRepresentation() {
		StateRepresentation<S, T> representation = config.getRepresentation(getState());
		return representation == null ? new StateRepresentation<>(getState()) : representation;
	}

	/**
	 * Transition from the current state via the specified trigger.
	 * The target state is determined by the configuration of the current state.
	 * Actions associated with leaving the current state and entering the new one
	 * will be invoked
	 *
	 * @param trigger The trigger to fire
	 */
	public void fire(T trigger) {
		publicFire(trigger);
	}

	/**
	 * Transition from the current state via the specified trigger.
	 * The target state is determined by the configuration of the current state.
	 * Actions associated with leaving the current state and entering the new one
	 * will be invoked.
	 *
	 * @param trigger The trigger to fire
	 * @param arg0    The first argument
	 * @param <TArg0> Type of the first trigger argument
	 */
	public <TArg0> void fire(
			final T trigger,
			final TArg0 arg0) {
		requireNonNull(trigger, "trigger is null");
		publicFire(trigger, arg0);
	}

	/**
	 * Transition from the current state via the specified trigger.
	 * The target state is determined by the configuration of the current state.
	 * Actions associated with leaving the current state and entering the new one
	 * will be invoked.
	 *
	 * @param trigger The trigger to fire
	 * @param arg0    The first argument
	 * @param <TArg0> Type of the first trigger argument
	 */
	public <TArg0> void fire(
			final TriggerWithParameters1<TArg0, S, T> trigger,
			final TArg0 arg0) {
		requireNonNull(trigger, "trigger is null");
		publicFire(trigger.getTrigger(), arg0);
	}

	/**
	 * Transition from the current state via the specified trigger.
	 * The target state is determined by the configuration of the current state.
	 * Actions associated with leaving the current state and entering the new one
	 * will be invoked.
	 *
	 * @param trigger The trigger to fire
	 * @param arg0    The first argument
	 * @param arg1    The second argument
	 * @param <TArg0> Type of the first trigger argument
	 * @param <TArg1> Type of the second trigger argument
	 */
	public <TArg0, TArg1> void fire(
			final TriggerWithParameters2<TArg0, TArg1, S, T> trigger,
			final TArg0 arg0,
			final TArg1 arg1) {
		requireNonNull(trigger, "trigger is null");
		publicFire(trigger.getTrigger(), arg0, arg1);
	}

	/**
	 * Transition from the current state via the specified trigger.
	 * The target state is determined by the configuration of the current state.
	 * Actions associated with leaving the current state and entering the new one
	 * will be invoked.
	 *
	 * @param trigger The trigger to fire
	 * @param arg0    The first argument
	 * @param arg1    The second argument
	 * @param arg2    The third argument
	 * @param <TArg0> Type of the first trigger argument
	 * @param <TArg1> Type of the second trigger argument
	 * @param <TArg2> Type of the third trigger argument
	 */
	public <TArg0, TArg1, TArg2> void fire(
			final TriggerWithParameters3<TArg0, TArg1, TArg2, S, T> trigger,
			final TArg0 arg0,
			final TArg1 arg1,
			final TArg2 arg2) {
		requireNonNull(trigger, "trigger is null");
		publicFire(trigger.getTrigger(), arg0, arg1, arg2);
	}

	protected void publicFire(final T trigger, final Object... args) {
		boolean triggerHandled = false;

		// check if the trigger is for parallel state machine and try to relay it
		if (getCurrentRepresentation().isParallelState()) {
			// collect all parallel state machines that can currently handle the trigger.
			List<StateMachine<S, T>> possibleStateMachines = parallelStateMachines.get(getCurrentRepresentation().getUnderlyingState()).stream()
					.filter(s -> s.getCurrentRepresentation().canHandle(trigger)).collect(Collectors.toList());
			// if found, relay the trigger
			if (!possibleStateMachines.isEmpty()) {
				triggerHandled = true;
				possibleStateMachines.forEach(s -> s.publicFire(trigger, args));
			}
		}

		// check if the trigger is handled by the current state.
		TriggerWithParameters<S, T> configuration = config.getTriggerConfiguration(trigger);
		if (configuration != null) {
			configuration.validateParameters(args);
		}

		TriggerBehaviour<S, T> triggerBehaviour = getCurrentRepresentation().tryFindHandler(trigger);
		if (triggerBehaviour != null) {
			triggerHandled = true;
			S source = getState();
			OutVar<S> destination = new OutVar<>();
			if (triggerBehaviour.resultsInTransitionFrom(source, args, destination)) {
				Transition<S, T> transition = new Transition<>(source, destination.get(), trigger);

				getCurrentRepresentation().exit(transition, args);
				triggerBehaviour.performAction(args);
				setState(destination.get());
				initializeParallelStateMachines(transition, args);
				getCurrentRepresentation().enter(transition, args);
			}
		}

		if (!triggerHandled) {
			unhandledTriggerAction.doIt(getCurrentRepresentation().getUnderlyingState(), trigger);
		}
	}

	private void initializeParallelStateMachines(Transition<S, T> transition, Object[] args) {
		// create parallel state machines if we are going to enter a parallel state
		// the machine is replaced with a new one every time we enter the state.
		if (getCurrentRepresentation().isParallelState()) {
			parallelStateMachines.put(getCurrentRepresentation().getUnderlyingState(), new ArrayList<>());
			getCurrentRepresentation().getParallelStateMachineConfigs().forEach(p -> parallelStateMachines.get(getCurrentRepresentation().getUnderlyingState())
					.add(createParallelStateMachine(p)));
		}
	}

	protected StateMachine<S, T> createParallelStateMachine(ParallelStateMachineConfig<S, T> parallelStateMachineConfig) {
		return new StateMachine<>(parallelStateMachineConfig.getInitialState(), parallelStateMachineConfig);
	}

	/**
	 * Override the default behaviour of throwing an exception when an unhandled trigger is fired
	 *
	 * @param unhandledTriggerAction An action to call when an unhandled trigger is fired
	 */
	public void onUnhandledTrigger(final Action2<S, T> unhandledTriggerAction) {
		if (unhandledTriggerAction == null) {
			throw new IllegalStateException("unhandledTriggerAction");
		}
		this.unhandledTriggerAction = unhandledTriggerAction;
	}

	/**
	 * Determine if the state machine is in the supplied state
	 *
	 * @param state The state to test for
	 * @return True if the current state is equal to, or a substate of, the supplied state
	 */
	public boolean isInState(final S state) {
		return getCurrentRepresentation().isIncludedIn(state);
	}

	/**
	 * Returns true if {@code trigger} can be fired  in the current state
	 *
	 * @param trigger Trigger to test
	 * @return True if the trigger can be fired, false otherwise
	 */
	public boolean canFire(final T trigger) {
		return getCurrentRepresentation().canHandle(trigger);
	}

	/**
	 * A human-readable representation of the state machine
	 *
	 * @return A description of the current state and permitted triggers
	 */
	@Override
	public String toString() {
		List<T> permittedTriggers = getPermittedTriggers();
		List<String> parameters = new ArrayList<>();

		for (T tTrigger : permittedTriggers) {
			parameters.add(tTrigger.toString());
		}

		StringBuilder params = new StringBuilder();
		String delim = "";
		for (String param : parameters) {
			params.append(delim);
			params.append(param);
			delim = ", ";
		}

		return String.format(
				"StateMachine {{ State = %s, PermittedTriggers = {{ %s }}}}",
				getState(),
				params.toString());
	}
}
