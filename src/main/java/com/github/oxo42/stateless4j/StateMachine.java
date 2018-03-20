package com.github.oxo42.stateless4j;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.oxo42.stateless4j.delegates.Action;
import com.github.oxo42.stateless4j.delegates.StateAccessor;
import com.github.oxo42.stateless4j.delegates.StateMutator;
import com.github.oxo42.stateless4j.transitions.Transition;
import com.github.oxo42.stateless4j.triggers.ParameterizedTrigger;
import com.github.oxo42.stateless4j.triggers.TriggerBehaviour;

/**
 * Models behaviour as transitions between a finite set of states
 *
 * @param <S> The type used to represent the states
 * @param <T> The type used to represent the triggers that cause state transitions
 */
public class StateMachine<S, T> {

    protected final StateMachineConfig<S, T> config;
    protected final StateAccessor<S> stateAccessor;
    protected final StateMutator<S> stateMutator;
    private final StateMachine<S, T> parentStateMachine;
    protected Action<S, T> unhandledTriggerAction = (stateMachineContext, transition, args) -> {
        throw new IllegalStateException(
                String.format(
                        "No valid leaving transitions are permitted from state '%s' for trigger '%s'. Consider ignoring the trigger.",
                        transition.getSource(), transition.getTrigger())
        );
    };

    private final StateMachineContext<S, T> stateMachineContext;
    private final Map<S, List<StateMachine<S, T>>> parallelStateMachines = new HashMap<>();

    /**
     * Construct a state machine
     *
     * @param initialState The initial state
     */
    public StateMachine(final S initialState) {
        this(initialState, new StateMachineConfig<>());
    }

    public StateMachine(final S initialState, final StateMachineConfig<S, T> config) {
        this(initialState, config, new DefaultStateMachineContext<>());
    }

    public StateMachine(final S initialState, final StateMachineConfig<S, T> config, StateMachineContext<S, T> context) {
        this(initialState, config, context, null);
    }

    /**
     * Construct a state machine
     *
     * @param initialState The initial state
     * @param config       State machine configuration
     */
    public StateMachine(final S initialState, final StateMachineConfig<S, T> config, final StateMachineContext<S, T> context, StateMachine<S, T> parentStateMachine) {
        this.config = config;
        this.stateMachineContext = context;
        this.parentStateMachine = parentStateMachine;
        final StateReference<S> reference = new StateReference<>();
        reference.setState(initialState);
        stateAccessor = reference::getState;
        stateMutator = reference::setState;
        if (context.getTopLevelStateMachine() == null) {
            context.setStateMachine(this);
        }

        if (config.isEntryActionOfInitialStateEnabled()) {
            Transition<S, T> initialTransition = new Transition<>(initialState, initialState, null);
            initializeParallelStateMachines();
            getCurrentRepresentation().initEnter(context, initialTransition);
        }
    }

    public StateMachine(final S initialState, final StateAccessor<S> stateAccessor, final StateMutator<S> stateMutator) {
        this(initialState, stateAccessor, stateMutator, new StateMachineConfig<>());
    }

    /**
     * Construct a state machine with external state storage.
     *
     * @param initialState  The initial state
     * @param stateAccessor State accessor
     * @param stateMutator  State mutator
     * @param config        State machine configuration
     */
    public StateMachine(final S initialState, final StateAccessor<S> stateAccessor, final StateMutator<S> stateMutator, final StateMachineConfig<S, T> config) {
        this(initialState, stateAccessor, stateMutator, config, new DefaultStateMachineContext<>());
    }

    public StateMachine(final S initialState, final StateAccessor<S> stateAccessor, final StateMutator<S> stateMutator, final StateMachineConfig<S, T>
            config, StateMachineContext<S,T> context) {
        this(initialState, stateAccessor, stateMutator, config, context, null);
    }

    public StateMachine(final S initialState, final StateAccessor<S> stateAccessor, final StateMutator<S> stateMutator, final StateMachineConfig<S, T>
            config, StateMachineContext<S, T> context, StateMachine<S, T> parentStateMachine) {
        this.config = config;
        this.stateMachineContext = context;
        this.parentStateMachine = parentStateMachine;
        this.stateAccessor = stateAccessor;
        this.stateMutator = stateMutator;
        stateMutator.accept(initialState);
        if (context.getTopLevelStateMachine() == null) {
            context.setStateMachine(this);
        }

        if (config.isEntryActionOfInitialStateEnabled()) {
            Transition<S, T> initialTransition = new Transition<>(initialState, initialState, null);
            initializeParallelStateMachines();
            getCurrentRepresentation().initEnter(context, initialTransition);
        }
    }

    public StateConfiguration<S, T> configure(final S state) {
        return config.configure(state);
    }

    public StateMachineConfig<S, T> configuration() {
        return config;
    }

    /**
     * Get the {@link StateMachineContext} of this contexted state machine. This context allows for the storing of context information for this state machine.
     *
     * @return the context of this state machine
     */
    public StateMachineContext<S, T> getStateMachineContext() {
        return stateMachineContext;
    }

    /**
     * The current state
     *
     * @return The current state
     */
    private S getState() {
        return stateAccessor.call();
    }

    private void setState(S value) {
        stateMutator.accept(value);
    }

    /**
     * Retrieves the current state of the state machine including all parallel states active.
     *
     * @return StateMachineState the state of the state machine.
     */
    public StateMachineState<S> getStateMachineState() {
        List<StateRepresentation<S, T>> path = resolvePathToTop(getCurrentRepresentation());
        return getStateMachineState(path);
    }

    private List<StateRepresentation<S, T>> resolvePathToTop(StateRepresentation<S, T> stateRepresentation) {
        List<StateRepresentation<S, T>> path = new ArrayList<>();
        path.add(stateRepresentation);
        StateRepresentation<S, T> next = stateRepresentation.getSuperstate();
        while (next != null && !next.isParallelState()) {
            path.add(0, next);
            next = next.getSuperstate();
        }
        return path;
    }

    private StateMachineState<S> getStateMachineState(List<StateRepresentation<S, T>> path) {
        if (path.size() == 1) {
            return getStateMachineState(path.get(0));
        }
        StateMachineState<S> state = getStateMachineState(path.remove(0));
        state.getSubStates().add(getStateMachineState(path));
        return state;
    }

    private StateMachineState<S> getStateMachineState(StateRepresentation<S, T> currentState) {
        S state = currentState.getUnderlyingState();
        StateMachineState<S> stateMachineState = new StateMachineState<>(currentState.getUnderlyingState());
        if (currentState.isParallelState()) {
            if (parallelStateMachines.containsKey(state)) {
                parallelStateMachines.get(state).forEach(psm -> {
                    stateMachineState.getSubStates().add(psm.getStateMachineState());
                });
            } else {
                throw new IllegalStateException("State " + currentState.getUnderlyingState() + " is parallel, but no parallel states found.");
            }
        }
        return stateMachineState;
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
     * will be invoked.
     *
     * @param trigger The trigger to fire
     * @param args the arguments
     */
    public void fire(
            final T trigger,
            final Object ... args) {
        requireNonNull(trigger, "trigger is null");
        publicFire(trigger, args);
    }


    protected void publicFire(final T trigger, final Object... args) {
        if (!privateFire(trigger, args)) {
            unhandledTriggerAction.doIt(stateMachineContext, new Transition<>(getCurrentRepresentation().getUnderlyingState(),null, trigger));
        }
    }

    private boolean privateFire(final T trigger, final Object... args) {
        boolean handled = false;

        // check if the trigger is handled by the current state.
        ParameterizedTrigger<S, T> configuration = config.getTriggerConfiguration(trigger);
        if (configuration != null) {
            configuration.validateParameters(args);
        }

        TriggerBehaviour<S, T> triggerBehaviour = getCurrentRepresentation().tryFindHandler(stateMachineContext, trigger, args);
        if (triggerBehaviour != null) {
            handled = true;
            S source = getState();
            OutVar<S> destination = new OutVar<>();
            if (triggerBehaviour.resultsInTransitionFrom(stateMachineContext,source, args, destination)) {
                Transition<S, T> transition = new Transition<>(source, destination.get(), trigger);

                getCurrentRepresentation().exit(stateMachineContext, transition, args);
                triggerBehaviour.performAction(stateMachineContext,args);
                setState(destination.get());
                initializeParallelStateMachines();
                getCurrentRepresentation().enter(stateMachineContext, transition, args);
            }
        }

        if (!handled) {
            // if not handled and we are a parallel state, relay to sub state machines.
            if (getCurrentRepresentation().isParallelState()) {
                // relay to all sub state machines and check if at least one of them accepted it.
                handled = parallelStateMachines.get(getCurrentRepresentation().getUnderlyingState()).stream()
                        .map(s -> s.privateFire(trigger, args)).collect(Collectors.toList()).contains(true);
            }
        }
        return handled;
    }

    private void initializeParallelStateMachines() {
        // create parallel state machines if we are going to enter a parallel state
        // the machine is replaced with a new one every time we enter the state.
        if (getCurrentRepresentation().isParallelState()) {
            final S underlyingState = getCurrentRepresentation().getUnderlyingState();
            parallelStateMachines.put(underlyingState, new ArrayList<>());
            getCurrentRepresentation().getParallelStateMachineConfigs().forEach(pc -> parallelStateMachines.get(underlyingState)
                    .add(createParallelStateMachine(pc, getCurrentRepresentation())));
        }
    }

    private StateMachine<S, T> createParallelStateMachine(ParallelStateMachineConfig<S, T> parallelStateMachineConfig, StateRepresentation<S, T> parent) {
        StateMachine<S, T> stateMachine = new StateMachine<>(parallelStateMachineConfig.getInitialState(), parallelStateMachineConfig, getStateMachineContext
                (),this);
        stateMachine.getTopLevelStates().forEach(s -> s.setSuperstate(parent));
        return stateMachine;
    }

    private List<StateRepresentation<S, T>> getTopLevelStates() {
        return config.getTopLevelStates().stream().map(config::getRepresentation).collect(Collectors.toList());
    }

    /**
     * Override the default behaviour of throwing an exception when an unhandled trigger is fired
     *
     * @param unhandledTriggerAction An action to call when an unhandled trigger is fired
     */
    public void onUnhandledTrigger(final Action<S, T> unhandledTriggerAction) {
        if (unhandledTriggerAction == null) {
            throw new IllegalStateException("unhandledTriggerAction");
        }
        this.unhandledTriggerAction = unhandledTriggerAction;
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

    public List<StateMachine<S, T>> getStateMachinesContainingState(S state) {
        if (config.getStates().contains(state)) {
            return Collections.singletonList(this);
        } else {
            List<S> parallelStates = config.getStates()
                    .stream()
                    .map(config::getRepresentation)
                    .filter(StateRepresentation::isParallelState)
                    .map(StateRepresentation::getUnderlyingState)
                    .collect(Collectors.toList());

            List<StateMachine<S, T>> result = new ArrayList<>();
            parallelStates.forEach(ps -> getParallelStateMachines(ps).forEach(psm -> result.addAll(psm.getStateMachinesContainingState(state))));
            return result;
        }
    }

    private List<StateMachine<S, T>> getParallelStateMachines(S state) {
        if (parallelStateMachines.containsKey(state)) {
            return parallelStateMachines.get(state);
        }
        throw new IllegalArgumentException("State " + state + " is not a parallel state.");
    }

    public StateMachine<S, T> getParentStateMachine() {
        return parentStateMachine;
    }
}
