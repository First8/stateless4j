package com.github.oxo42.stateless4j;

import com.github.oxo42.stateless4j.delegates.Action;
import com.github.oxo42.stateless4j.delegates.Func;
import com.github.oxo42.stateless4j.transitions.TransitioningTriggerBehaviour;
import com.github.oxo42.stateless4j.triggers.DynamicTriggerBehaviour;
import com.github.oxo42.stateless4j.triggers.IgnoredTriggerBehaviour;
import com.github.oxo42.stateless4j.triggers.ParameterizedTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class StateConfiguration<S, T> {

	private final Func<Boolean, S, T> NO_GUARD = (context, args) -> true;
	private final Action<S, T> NO_ACTION = (stateMachineContext, t, args) -> {
	};
	private final StateRepresentation<S, T> representation;
	private final Function<S, StateRepresentation<S, T>> lookup;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public StateConfiguration(final StateRepresentation<S, T> representation, final Function<S,
			StateRepresentation<S, T>> lookup) {
		this.representation = requireNonNull(representation, "representation is null");
		this.lookup = requireNonNull(lookup, "lookup is null");
	}

	/**
	 * Accept the specified trigger and transition to the destination state
	 *
	 * @param trigger          The accepted trigger
	 * @param destinationState The state that the trigger will cause a transition to
	 * @return The reciever
	 */
	public StateConfiguration<S, T> permit(final T trigger, final S destinationState) {
		enforceNotIdentityTransition(destinationState);
		return publicPermit(trigger, destinationState);
	}

	/**
	 * Accept the specified trigger and transition to the destination state.
	 * <p>
	 * Additionally a given action is performed when transitioning. This action will be called after
	 * the onExit action of the current state and before the onEntry action of
	 * the destination state.
	 *
	 * @param trigger          The accepted trigger
	 * @param destinationState The state that the trigger will cause a transition to
	 * @param action           The action to be performed "during" transition
	 * @return The reciever
	 */
	public StateConfiguration<S, T> permit(final T trigger, final S destinationState, final Action<S,T> action) {
		enforceNotIdentityTransition(destinationState);
		return publicPermit(trigger, destinationState, action);
	}

	/**
	 * Accept the specified trigger and transition to the destination state
	 *
	 * @param trigger          The accepted trigger
	 * @param destinationState The state that the trigger will cause a transition to
	 * @param guard            Function that must return true in order for the trigger to be accepted
	 * @return The reciever
	 */
	public StateConfiguration<S, T> permitIf(final T trigger, final S destinationState, final Func<Boolean,S,T> guard) {
		enforceNotIdentityTransition(destinationState);
		return publicPermitIf(trigger, destinationState, guard);
	}

	/**
	 * Accept the specified trigger and transition to the destination state
	 * <p>
	 * Additionally a given action is performed when transitioning. This action will be called after
	 * the onExit action of the current state and before the onEntry action of
	 * the destination state.
	 *
	 * @param trigger          The accepted trigger
	 * @param destinationState The state that the trigger will cause a transition to
	 * @param guard            Function that must return true in order for the trigger to be accepted
	 * @param action           The action to be performed "during" transition
	 * @return The reciever
	 */
	public StateConfiguration<S, T> permitIf(
			final T trigger,
			final S destinationState,
			final Func<Boolean,S,T> guard,
			final Action<S,T> action) {
		enforceNotIdentityTransition(destinationState);
		return publicPermitIf(trigger, destinationState, guard, action);
	}

	/**
	 * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the
	 * configured state transitions to an identical sibling state
	 * <p>
	 * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute
	 * transitioning between super- and sub-states
	 *
	 * @param trigger The accepted trigger
	 * @return The reciever
	 */
	public StateConfiguration<S, T> permitReentry(final T trigger) {
		return publicPermit(trigger, representation.getUnderlyingState());
	}

	/**
	 * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the
	 * configured state transitions to an identical sibling state
	 * <p>
	 * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute
	 * transitioning between super- and sub-states
	 * <p>
	 * Additionally a given action is performed when transitioning. This action will be called after
	 * the onExit action and before the onEntry action (of the re-entered state).
	 *
	 * @param trigger The accepted trigger
	 * @param action  The action to be performed "during" transition
	 * @return The reciever
	 */
	public StateConfiguration<S, T> permitReentry(final T trigger, final Action<S,T> action) {
		return publicPermit(trigger, representation.getUnderlyingState(), action);
	}

	/**
	 * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the
	 * configured state transitions to an identical sibling state
	 * <p>
	 * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute
	 * transitioning between super- and sub-states
	 *
	 * @param trigger The accepted trigger
	 * @param guard   Function that must return true in order for the trigger to be accepted
	 * @return The reciever
	 */
	public StateConfiguration<S, T> permitReentryIf(final T trigger, final Func<Boolean,S,T> guard) {
		return publicPermitIf(trigger, representation.getUnderlyingState(), guard);
	}

	/**
	 * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the
	 * configured state transitions to an identical sibling state
	 * <p>
	 * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute
	 * transitioning between super- and sub-states
	 * <p>
	 * Additionally a given action is performed when transitioning. This action will be called after
	 * the onExit action and before the onEntry action (of the re-entered state).
	 *
	 * @param trigger The accepted trigger
	 * @param guard   Function that must return true in order for the trigger to be accepted
	 * @param action  The transition action to execute in the case of the re-entry.
	 * @return The reciever
	 */
	public StateConfiguration<S, T> permitReentryIf(final T trigger, final Func<Boolean, S, T> guard, final Action<S,T> action) {
		return publicPermitIf(trigger, representation.getUnderlyingState(), guard, action);
	}

	/**
	 * ignore the specified trigger when in the configured state
	 *
	 * @param trigger The trigger to ignore
	 * @return The receiver
	 */
	public StateConfiguration<S, T> ignore(final T trigger) {
		return ignoreIf(trigger, NO_GUARD);
	}

	/**
	 * ignore the specified trigger when in the configured state, if the guard returns true
	 *
	 * @param trigger The trigger to ignore
	 * @param guard   Function that must return true in order for the trigger to be ignored
	 * @return The receiver
	 */
	public StateConfiguration<S, T> ignoreIf(final T trigger, final Func<Boolean, S, T> guard) {
		requireNonNull(guard, "guard is null");
		representation.addTriggerBehaviour(new IgnoredTriggerBehaviour<>(trigger, guard));
		return this;
	}


	/**
	 * Specify an action that will execute when transitioning into the configured state
	 *
	 * @param entryAction Action to execute, providing details of the transition
	 * @return The receiver
	 */
	public StateConfiguration<S, T> onEntry(final Action<S, T> entryAction) {
		requireNonNull(entryAction, "entryAction is null");
		representation.addEntryAction(entryAction);
		return this;
	}

	/**
	 * Specify an action that will execute when transitioning into the configured state
	 *
	 * @param trigger     The trigger by which the state must be entered in order for the action to execute
	 * @param entryAction Action to execute, providing details of the transition
	 * @return The receiver
	 */
	public StateConfiguration<S, T> onEntryFrom(final T trigger, final Action<S, T> entryAction) {
		requireNonNull(entryAction, "entryAction is null");
		representation.addEntryAction(entryAction, new ParameterizedTrigger<>(trigger));
		return this;
	}

	/**
	 * Specify an action that will execute when transitioning into the configured state
	 *
	 * @param trigger     The trigger by which the state must be entered in order for the action to execute
	 * @param entryAction Action to execute, providing details of the transition
	 * @return The receiver
	 */
	public StateConfiguration<S, T> onEntryFrom(final ParameterizedTrigger<S, T> trigger, final Action<S, T> entryAction) {
		requireNonNull(entryAction, "entryAction is null");
		representation.addEntryAction(entryAction, trigger);
		return this;
	}


	/**
	 * Specify an action that will execute when transitioning from the configured state
	 *
	 * @param exitAction Action to execute
	 * @return The receiver
	 */
	public StateConfiguration<S, T> onExit(final Action<S, T> exitAction) {
		requireNonNull(exitAction, "exitAction is null");
		representation.addExitAction(exitAction);
		return this;
	}

	/**
	 * Sets the superstate that the configured state is a substate of
	 * <p>
	 * Substates inherit the allowed transitions of their superstate.
	 * When entering directly into a substate from outside of the superstate,
	 * entry actions for the superstate are executed.
	 * Likewise when leaving from the substate to outside the supserstate,
	 * exit actions for the superstate will execute.
	 *
	 * @param superstate The superstate
	 * @return The receiver
	 */
	public StateConfiguration<S, T> substateOf(final S superstate) {
		StateRepresentation<S, T> superRepresentation = lookup.apply(superstate);
		representation.setSuperstate(superRepresentation);
		superRepresentation.addSubstate(representation);
		return this;
	}

	/**
	 * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
	 * function
	 *
	 * @param trigger                  The accepted trigger
	 * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
	 * @return The reciever
	 */
	public StateConfiguration<S, T> permitDynamic(
			final T trigger,
			final Func<S, S, T> destinationStateSelector) {
		return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD);
	}

	/**
	 * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
	 * function
	 * <p>
	 * Additionally a given action is performed when transitioning. This action will be called after
	 * the onExit action and before the onEntry action (of the re-entered state).
	 *
	 * @param trigger                  The accepted trigger
	 * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
	 * @param action                   The action to be performed "during" transition
	 * @return The receiver
	 */
	public StateConfiguration<S, T> permitDynamic(
			final T trigger,
			final Func<S, S, T> destinationStateSelector,
			final Action<S, T> action) {
		return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD, action);
	}

	/**
	 * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
	 * function
	 *
	 * @param trigger                  The accepted trigger
	 * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
	 * @return The receiver
	 */
	public StateConfiguration<S, T> permitDynamic(
			final ParameterizedTrigger<S, T> trigger,
			final Func<S, S, T> destinationStateSelector) {
		return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD);
	}

	/**
	 * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
	 * function
	 *
	 * @param trigger                  The accepted trigger
	 * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
	 * @param guard                    Function that must return true in order for the  trigger to be accepted
	 * @return The reciever
	 */
	public StateConfiguration<S, T> permitDynamicIf(
			final T trigger,
			final Func<S, S, T> destinationStateSelector,
			final Func<Boolean, S, T> guard) {
		requireNonNull(destinationStateSelector, "destinationStateSelector is null");
		return publicPermitDynamicIf(
				trigger,
				destinationStateSelector,
				guard,
				NO_ACTION);
	}

	/**
	 * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
	 * function
	 * <p>
	 * Additionally a given action is performed when transitioning. This action will be called after
	 * the onExit action of the current state and before the onEntry action of the destination state.
	 *
	 * @param trigger                  The accepted trigger
	 * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
	 * @param guard                    Function that must return true in order for the  trigger to be accepted
	 * @param action                   The action to be performed "during" transition
	 * @return The receiver
	 */
	public StateConfiguration<S, T> permitDynamicIf(
			final T trigger,
			final Func<S, S, T> destinationStateSelector,
			final Func<Boolean, S, T> guard,
			final Action<S, T> action) {
		requireNonNull(destinationStateSelector, "destinationStateSelector is null");
		return publicPermitDynamicIf(
				trigger,
				destinationStateSelector,
				guard,
				action);
	}

	/**
	 * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
	 * function
	 *
	 * @param trigger                  The accepted trigger
	 * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
	 * @param guard                    Function that must return true in order for the  trigger to be accepted
	 * @return The reciever
	 */
	@SuppressWarnings("unchecked")
	public StateConfiguration<S, T> permitDynamicIf(
			final ParameterizedTrigger<S, T> trigger,
			final Func<S, S, T> destinationStateSelector,
			final Func<Boolean, S, T> guard) {
		requireNonNull(trigger, "trigger is null");
		requireNonNull(destinationStateSelector, "destinationStateSelector is null");
		return publicPermitDynamicIf(
				trigger.getTrigger(),
				destinationStateSelector,
				guard,
				NO_ACTION);
	}

	/**
	 * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
	 * function
	 * <p>
	 * Additionally a given action is performed when transitioning. This action will be called after
	 * the onExit action of the current state and before the onEntry action of the destination state.
	 * The parameter of the trigger will be given to this action.
	 *
	 * @param trigger                  The accepted trigger
	 * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
	 * @param guard                    Function that must return true in order for the  trigger to be accepted
	 * @param action                   The action to be performed "during" transition
	 * @return The receiver
	 */
	@SuppressWarnings("unchecked")
	public StateConfiguration<S, T> permitDynamicIf(
			final ParameterizedTrigger<S, T> trigger,
			final Func<S, S, T> destinationStateSelector,
			final Func<Boolean, S, T> guard,
			final Action<S, T> action) {
		requireNonNull(trigger, "trigger is null");
		requireNonNull(destinationStateSelector, "destinationStateSelector is null");
		return publicPermitDynamicIf(
				trigger.getTrigger(),
				destinationStateSelector,
				guard,
				action);
	}


	private void enforceNotIdentityTransition(final S destination) {
		if (destination.equals(representation.getUnderlyingState())) {
			throw new IllegalStateException("Permit() (and PermitIf()) require that the destination state is not equal to the source state. "
					+ "To accept a trigger without changing state, use either Ignore() or PermitReentry().");
		}
	}

	StateConfiguration<S, T> publicPermit(final T trigger, final S destinationState) {
		return publicPermitIf(trigger, destinationState, NO_GUARD, NO_ACTION);
	}

	StateConfiguration<S, T> publicPermit(T trigger, S destinationState, Action<S,T> action) {
		return publicPermitIf(trigger, destinationState, NO_GUARD, action);
	}

	StateConfiguration<S, T> publicPermitIf(T trigger, S destinationState, Func<Boolean,S,T>  guard) {
		return publicPermitIf(trigger, destinationState, guard, NO_ACTION);
	}

	StateConfiguration<S, T> publicPermitIf(T trigger, S destinationState, Func<Boolean,S,T> guard, Action<S,T> action) {
		requireNonNull(guard, "guard is null");
		requireNonNull(action, "action is null");
		representation.addTriggerBehaviour(new TransitioningTriggerBehaviour<>(trigger, destinationState, guard, action));
		return this;
	}

	StateConfiguration<S, T> publicPermitDynamic(final T trigger, final Func<S, S, T> destinationStateSelector) {
		return publicPermitDynamicIf(trigger, destinationStateSelector, NO_GUARD, NO_ACTION);
	}

	StateConfiguration<S, T> publicPermitDynamicIf(
			final T trigger,
			final Func<S, S, T> destinationStateSelector,
			final Func<Boolean, S, T> guard,
			final Action<S, T> action) {
		requireNonNull(destinationStateSelector, "destinationStateSelector is null");
		requireNonNull(guard, "guard is null");
		representation.addTriggerBehaviour(new DynamicTriggerBehaviour<>(trigger, destinationStateSelector, guard, action));
		return this;
	}

	public StateConfiguration<S, T> parallel(ParallelStateMachineConfig<S, T> stateMachineConfig) {
		requireNonNull(stateMachineConfig, "stateMachineConfig is null");
		representation.addParallelStateMachineConfig(stateMachineConfig);
		return this;
	}
}
