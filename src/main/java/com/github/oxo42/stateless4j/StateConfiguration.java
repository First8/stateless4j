package com.github.oxo42.stateless4j;

import com.github.oxo42.stateless4j.delegates.*;
import com.github.oxo42.stateless4j.transitions.Transition;
import com.github.oxo42.stateless4j.transitions.TransitioningTriggerBehaviour;
import com.github.oxo42.stateless4j.triggers.*;

import static java.util.Objects.requireNonNull;

public class StateConfiguration<S, T> {

    private static final FuncBoolean NO_GUARD = () -> true;
    private static final Action NO_ACTION = () -> { };
    private static final Action1<Object[]> NO_ACTION_N = args -> { };
    private final StateRepresentation<S, T> representation;
    private final Func2<S, StateRepresentation<S, T>> lookup;

    public StateConfiguration(final StateRepresentation<S, T> representation, final Func2<S, StateRepresentation<S, T>> lookup) {
        this.representation = requireNonNull(representation, "representation is null");
        this.lookup = requireNonNull(lookup,  "lookup is null");
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
     *
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action of the current state and before the onEntry action of
     * the destination state.
     *
     * @param trigger          The accepted trigger
     * @param destinationState The state that the trigger will cause a transition to
     * @param action           The action to be performed "during" transition
     * @return The reciever
     */
    public StateConfiguration<S, T> permit(final T trigger, final S destinationState, final Action action) {
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
    public StateConfiguration<S, T> permitIf(final T trigger, final S destinationState, final FuncBoolean guard) {
        enforceNotIdentityTransition(destinationState);
        return publicPermitIf(trigger, destinationState, guard);
    }

    /**
     * Accept the specified trigger and transition to the destination state
     *
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
            final FuncBoolean guard,
            final Action action) {
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
    public StateConfiguration<S, T> permitReentry(final T trigger, final Action action) {
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
    public StateConfiguration<S, T> permitReentryIf(final T trigger, final FuncBoolean guard) {
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
    public StateConfiguration<S, T> permitReentryIf(final T trigger, final FuncBoolean guard, final Action action) {
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
    public StateConfiguration<S, T> ignoreIf(final T trigger, final FuncBoolean guard) {
        requireNonNull(guard, "guard is null");
        representation.addTriggerBehaviour(new IgnoredTriggerBehaviour<>(trigger, guard));
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param entryAction Action to execute
     * @return The receiver
     */
    public StateConfiguration<S, T> onEntry(final Action entryAction) {
        requireNonNull(entryAction, "entryAction is null");
        return onEntry(t -> entryAction.doIt());
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param entryAction Action to execute, providing details of the transition
     * @return The receiver
     */
    public StateConfiguration<S, T> onEntry(final Action1<Transition<S, T>> entryAction) {
        requireNonNull(entryAction, "entryAction is null");
        representation.addEntryAction((transition, args) -> entryAction.doIt(transition));
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @return The receiver
     */
    @SuppressWarnings("unchecked")
    public <TArg0> StateConfiguration<S, T> onEntry(
            final Action2<TArg0, Transition<S, T>> entryAction,
            final Class<TArg0> classe0) {
        requireNonNull(entryAction, "entryAction is null");
        representation.addEntryAction((transition, args) -> entryAction.doIt((TArg0) args[0], transition));
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute
     * @return The receiver
     */
    public StateConfiguration<S, T> onEntryFrom(final T trigger, final Action entryAction) {
        requireNonNull(entryAction, "entryAction is null");
        return onEntryFrom(trigger, transition -> entryAction.doIt());
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @return The receiver
     */
    public StateConfiguration<S, T> onEntryFrom(final T trigger, final Action1<Transition<S, T>> entryAction) {
        requireNonNull(entryAction, "entryAction is null");
        representation.addEntryAction(trigger, (transition, args) -> entryAction.doIt(transition));
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @return The receiver
     */
    public <TArg0> StateConfiguration<S, T> onEntryFrom(
            final TriggerWithParameters1<TArg0, S, T> trigger,
            final Action1<TArg0> entryAction,
            final Class<TArg0> classe0) {
        requireNonNull(entryAction, "entryAction is null");
        return onEntryFrom(trigger, (transition, args) -> entryAction.doIt(transition), classe0);
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @return The receiver
     */
    @SuppressWarnings("unchecked")
    public <TArg0> StateConfiguration<S, T> onEntryFrom(
            final TriggerWithParameters1<TArg0, S, T> trigger,
            final Action2<TArg0, Transition<S, T>> entryAction,
            final Class<TArg0> classe0) {
        requireNonNull(trigger, "trigger is null");
        requireNonNull(entryAction, "entryAction is null");
        representation.addEntryAction(
                trigger.getTrigger(),
                (transition, args) -> entryAction.doIt((TArg0) args[0], transition));
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param classe1     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @param <TArg1>     Type of the second trigger argument
     * @return The receiver
     */
    public <TArg0, TArg1> StateConfiguration<S, T> onEntryFrom(
            final TriggerWithParameters2<TArg0, TArg1, S, T> trigger,
            final Action2<TArg0, TArg1> entryAction,
            final Class<TArg0> classe0,
            final Class<TArg1> classe1) {
        requireNonNull(entryAction, "entryAction is null");
        return onEntryFrom(trigger, (arg0, arg1, transition) -> entryAction.doIt(arg0, arg1), classe0, classe1);
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param classe1     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @param <TArg1>     Type of the second trigger argument
     * @return The receiver
     */
    @SuppressWarnings("unchecked")
    public <TArg0, TArg1> StateConfiguration<S, T> onEntryFrom(
            final TriggerWithParameters2<TArg0, TArg1, S, T> trigger,
            final Action3<TArg0, TArg1, Transition<S, T>> entryAction,
            final Class<TArg0> classe0,
            final Class<TArg1> classe1) {
        requireNonNull(trigger, "trigger is null");
        requireNonNull(entryAction, "entryAction is null");
        representation.addEntryAction(trigger.getTrigger(), (t, args) -> entryAction.doIt(
                (TArg0) args[0],
                (TArg1) args[1], t));
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param classe1     Class argument
     * @param classe2     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @param <TArg1>     Type of the second trigger argument
     * @param <TArg2>     Type of the third trigger argument
     * @return The receiver
     */
    public <TArg0, TArg1, TArg2> StateConfiguration<S, T> onEntryFrom(
            final TriggerWithParameters3<TArg0, TArg1, TArg2, S, T> trigger,
            final Action3<TArg0, TArg1, TArg2> entryAction,
            final Class<TArg0> classe0,
            final Class<TArg1> classe1,
            final Class<TArg2> classe2) {
        requireNonNull(entryAction, "entryAction is null");
        return onEntryFrom(
                trigger,
                (arg0, arg1, arg2, transition) -> entryAction.doIt(arg0, arg1, arg2),
                classe0,
                classe1,
                classe2);
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @param classe0     Class argument
     * @param classe1     Class argument
     * @param classe2     Class argument
     * @param <TArg0>     Type of the first trigger argument
     * @param <TArg1>     Type of the second trigger argument
     * @param <TArg2>     Type of the third trigger argument
     * @return The receiver
     */
    @SuppressWarnings("unchecked")
    public <TArg0, TArg1, TArg2> StateConfiguration<S, T> onEntryFrom(
            final TriggerWithParameters3<TArg0, TArg1, TArg2, S, T> trigger,
            final Action4<TArg0, TArg1, TArg2, Transition<S, T>> entryAction,
            final Class<TArg0> classe0,
            final Class<TArg1> classe1,
            final Class<TArg2> classe2) {
        requireNonNull(trigger, "trigger is null");
        requireNonNull(entryAction, "entryAction is null");
        representation.addEntryAction(
                trigger.getTrigger(),
                (transition, args) -> entryAction.doIt(
                        (TArg0) args[0],
                        (TArg1) args[1],
                        (TArg2) args[2],
                        transition));
        return this;
    }

    /**
     * Specify an action that will execute when transitioning from the configured state
     *
     * @param exitAction Action to execute
     * @return The receiver
     */
    public StateConfiguration<S, T> onExit(final Action exitAction) {
        requireNonNull(exitAction, "exitAction is null");
        return onExit(transition -> exitAction.doIt());
    }

    /**
     * Specify an action that will execute when transitioning from the configured state
     *
     * @param exitAction Action to execute
     * @return The receiver
     */
    public StateConfiguration<S, T> onExit(final Action1<Transition<S, T>> exitAction) {
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
        StateRepresentation<S, T> superRepresentation = lookup.call(superstate);
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
            final Func<S> destinationStateSelector) {
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
            final Func<S> destinationStateSelector,
            final Action action) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD, action);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param <TArg0>                  Type of the first trigger argument
     * @return The receiver
     */
    public <TArg0> StateConfiguration<S, T> permitDynamic(
            final TriggerWithParameters1<TArg0, S, T> trigger,
            final Func2<TArg0, S> destinationStateSelector) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action and before the onEntry action (of the re-entered state). The parameter of the
     * trigger will be given to this action.
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param action                   The action to be performed "during" transition
     * @param <TArg0>                  Type of the first trigger argument
     * @return The receiver
     */
    public <TArg0> StateConfiguration<S, T> permitDynamic(
            final TriggerWithParameters1<TArg0, S, T> trigger,
    		final Func2<TArg0, S> destinationStateSelector,
            final Action1<TArg0> action) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD, action);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @return The receiver
     */
    public <TArg0, TArg1> StateConfiguration<S, T> permitDynamic(
            final TriggerWithParameters2<TArg0, TArg1, S, T> trigger,
            final Func3<TArg0, TArg1, S> destinationStateSelector) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD);
    }


    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action and before the onEntry action (of the re-entered state). The parameters of the
     * trigger will be given to this action.
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param action                   The action to be performed "during" transition
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @return The receiver
     */
    public <TArg0, TArg1> StateConfiguration<S, T> permitDynamic(
            final TriggerWithParameters2<TArg0, TArg1, S, T> trigger,
            final Func3<TArg0, TArg1, S> destinationStateSelector,
            final Action2<TArg0, TArg1> action) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD, action);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @param <TArg2>                  Type of the third trigger argument
     * @return The receiver
     */
    public <TArg0, TArg1, TArg2> StateConfiguration<S, T> permitDynamic(
            final TriggerWithParameters3<TArg0, TArg1, TArg2, S, T> trigger,
            final Func4<TArg0, TArg1, TArg2, S> destinationStateSelector) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD);
    }


    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action and before the onEntry action (of the re-entered state). The parameters of the
     * trigger will be given to this action.
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param action                   The action to be performed "during" transition
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @param <TArg2>                  Type of the third trigger argument
     * @return The receiver
     */
    public <TArg0, TArg1, TArg2> StateConfiguration<S, T> permitDynamic(
            final TriggerWithParameters3<TArg0, TArg1, TArg2, S, T> trigger,
    		final Func4<TArg0, TArg1, TArg2, S> destinationStateSelector,
    		final Action3<TArg0, TArg1, TArg2> action) {
        return permitDynamicIf(trigger, destinationStateSelector, NO_GUARD, action);
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
            final Func<S> destinationStateSelector,
            final FuncBoolean guard) {
        requireNonNull(destinationStateSelector, "destinationStateSelector is null");
        return publicPermitDynamicIf(
                trigger,
                arg0 -> destinationStateSelector.call(),
                guard,
                NO_ACTION_N);
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
            final Func<S> destinationStateSelector,
            final FuncBoolean guard,
    		final Action action) {
        requireNonNull(destinationStateSelector, "destinationStateSelector is null");
        return publicPermitDynamicIf(
                trigger,
                arg0 -> destinationStateSelector.call(),
                guard,
                args -> action.doIt());
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     * @param <TArg0>                  Type of the first trigger argument
     * @return The reciever
     */
    @SuppressWarnings("unchecked")
    public <TArg0> StateConfiguration<S, T> permitDynamicIf(
            final TriggerWithParameters1<TArg0, S, T> trigger,
            final Func2<TArg0, S> destinationStateSelector,
            final FuncBoolean guard) {
        requireNonNull(trigger , "trigger is null");
        requireNonNull(destinationStateSelector , "destinationStateSelector is null");
        return publicPermitDynamicIf(
                trigger.getTrigger(),
                args-> destinationStateSelector.call((TArg0) args[0]),
                guard,
                NO_ACTION_N);
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
     * @param <TArg0>                  Type of the first trigger argument
     * @return The receiver
     */
    @SuppressWarnings("unchecked")
    public <TArg0> StateConfiguration<S, T> permitDynamicIf(
            final TriggerWithParameters1<TArg0, S, T> trigger,
            final Func2<TArg0, S> destinationStateSelector,
            final FuncBoolean guard,
    		final Action1<TArg0> action) {
        requireNonNull(trigger , "trigger is null");
        requireNonNull(destinationStateSelector , "destinationStateSelector is null");
        return publicPermitDynamicIf(
                trigger.getTrigger(),
                args -> destinationStateSelector.call((TArg0) args[0]),
                guard,
                args -> action.doIt((TArg0) args[0]));
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @return The reciever
     */
    @SuppressWarnings("unchecked")
    public <TArg0, TArg1> StateConfiguration<S, T> permitDynamicIf(
            final TriggerWithParameters2<TArg0, TArg1, S, T> trigger,
            final Func3<TArg0, TArg1, S> destinationStateSelector,
            final FuncBoolean guard) {
        requireNonNull(trigger , "trigger is null");
        requireNonNull(destinationStateSelector , "destinationStateSelector is null");
        return publicPermitDynamicIf(
                trigger.getTrigger(),
                args-> destinationStateSelector.call(
                        (TArg0) args[0],
                        (TArg1) args[1]),
                guard,
                NO_ACTION_N);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action of the current state and before the onEntry action of the destination state.
     * The parameters of the trigger will be given to this action.
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     * @param action                   The action to be performed "during" transition
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @return The receiver
     */
    @SuppressWarnings("unchecked")
    public <TArg0, TArg1> StateConfiguration<S, T> permitDynamicIf(
            final TriggerWithParameters2<TArg0, TArg1, S, T> trigger,
            final Func3<TArg0, TArg1, S> destinationStateSelector,
            final FuncBoolean guard,
    		final Action2<TArg0, TArg1> action) {
        requireNonNull(trigger , "trigger is null");
        requireNonNull(destinationStateSelector , "destinationStateSelector is null");
        return publicPermitDynamicIf(
                trigger.getTrigger(),
                args -> destinationStateSelector.call(
                        (TArg0) args[0],
                        (TArg1) args[1]),
                guard,
                args -> action.doIt(
                        (TArg0) args[0],
                        (TArg1) args[1]));
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @param <TArg2>                  Type of the third trigger argument
     * @return The reciever
     */
    @SuppressWarnings("unchecked")
    public <TArg0, TArg1, TArg2> StateConfiguration<S, T> permitDynamicIf(
            final TriggerWithParameters3<TArg0, TArg1, TArg2, S, T> trigger,
            final Func4<TArg0, TArg1, TArg2, S> destinationStateSelector,final FuncBoolean guard) {
        requireNonNull(trigger , "trigger is null");
        requireNonNull(destinationStateSelector , "destinationStateSelector is null");
        return publicPermitDynamicIf(
                trigger.getTrigger(),
                args-> destinationStateSelector.call(
                        (TArg0) args[0],
                        (TArg1) args[1],
                        (TArg2) args[2]),
                guard,
                NO_ACTION_N);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function.
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action of the current state and before the onEntry action of the destination state.
     * The parameters of the trigger will be given to this action.
     *
     * @param trigger                  The accepted trigger
     * @param destinationStateSelector Function to calculate the state that the trigger will cause a transition to
     * @param guard                    Function that must return true in order for the  trigger to be accepted
     * @param action                   The action to be performed "during" transition
     * @param <TArg0>                  Type of the first trigger argument
     * @param <TArg1>                  Type of the second trigger argument
     * @param <TArg2>                  Type of the third trigger argument
     * @return The reciever
     */
    @SuppressWarnings("unchecked")
    public <TArg0, TArg1, TArg2> StateConfiguration<S, T> permitDynamicIf(
            final TriggerWithParameters3<TArg0, TArg1, TArg2, S, T> trigger,
            final Func4<TArg0, TArg1, TArg2, S> destinationStateSelector,
            final FuncBoolean guard,
            final Action3<TArg0, TArg1, TArg2> action) {
        requireNonNull(trigger , "trigger is null");
        requireNonNull(destinationStateSelector , "destinationStateSelector is null");
        return publicPermitDynamicIf(
                trigger.getTrigger(),
                args -> destinationStateSelector.call(
                        (TArg0) args[0],
                        (TArg1) args[1],
                        (TArg2) args[2]),
                guard,
                args -> action.doIt(
                        (TArg0) args[0],
                        (TArg1) args[1],
                        (TArg2) args[2]));
    }

    void enforceNotIdentityTransition(final S destination) {
        if (destination.equals(representation.getUnderlyingState())) {
            throw new IllegalStateException("Permit() (and PermitIf()) require that the destination state is not equal to the source state. "
                    + "To accept a trigger without changing state, use either Ignore() or PermitReentry().");
        }
    }

    StateConfiguration<S, T> publicPermit(final T trigger, final S destinationState) {
        return publicPermitIf(trigger, destinationState, NO_GUARD, NO_ACTION);
    }

    StateConfiguration<S, T> publicPermit(T trigger, S destinationState, Action action) {
        return publicPermitIf(trigger, destinationState, NO_GUARD, action);
    }

    StateConfiguration<S, T> publicPermitIf(T trigger, S destinationState, FuncBoolean guard) {
        return publicPermitIf(trigger, destinationState, guard, NO_ACTION);
    }

    StateConfiguration<S, T> publicPermitIf(T trigger, S destinationState, FuncBoolean guard, Action action) {
        requireNonNull(guard, "guard is null");
        requireNonNull(action, "action is null");
        representation.addTriggerBehaviour(new TransitioningTriggerBehaviour<>(trigger, destinationState, guard, action));
        return this;
    }

    StateConfiguration<S, T> publicPermitDynamic(final T trigger, final Func2<Object[], S> destinationStateSelector) {
        return publicPermitDynamicIf(trigger, destinationStateSelector, NO_GUARD, NO_ACTION_N);
    }

    StateConfiguration<S, T> publicPermitDynamicIf(
            final T trigger,
            final Func2<Object[], S> destinationStateSelector,
            final FuncBoolean guard,
            final Action1<Object[]> action) {
        requireNonNull(destinationStateSelector, "destinationStateSelector is null");
        requireNonNull(guard, "guard is null");
        representation.addTriggerBehaviour(new DynamicTriggerBehaviour<>(trigger, destinationStateSelector, guard, action));
        return this;
    }
}
