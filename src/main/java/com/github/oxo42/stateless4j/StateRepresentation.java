package com.github.oxo42.stateless4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.oxo42.stateless4j.delegates.Action2;
import com.github.oxo42.stateless4j.transitions.Transition;
import com.github.oxo42.stateless4j.triggers.TriggerBehaviour;

public class StateRepresentation<S, T> {

    private final S state;

    private final Map<T, List<TriggerBehaviour<S, T>>> triggerBehaviours = new HashMap<>();
    private final List<Action2<Transition<S, T>, Object[]>> entryActions = new ArrayList<>();
    private final List<Action2<Transition<S, T>, Object[]>> exitActions = new ArrayList<>();
    private final List<StateRepresentation<S, T>> substates = new ArrayList<>();
    private StateRepresentation<S, T> superstate; // null
    private List<ParallelStateMachineConfig<S,T>> parallelStates = new ArrayList<>();

    public StateRepresentation(S state) {
        this.state = state;
    }

    protected Map<T, List<TriggerBehaviour<S, T>>> getTriggerBehaviours() {
        return triggerBehaviours;
    }

    public Boolean canHandle(T trigger) {
        return tryFindHandler(trigger) != null;
    }

    public TriggerBehaviour<S, T> tryFindHandler(T trigger) {
        TriggerBehaviour<S, T> result = tryFindLocalHandler(trigger);
        if (result == null && superstate != null) {
            result = superstate.tryFindHandler(trigger);
        }
        return result;
    }

    TriggerBehaviour<S, T> tryFindLocalHandler(T trigger/*, out TriggerBehaviour handler*/) {
        List<TriggerBehaviour<S, T>> possible = triggerBehaviours.get(trigger);
        if (possible == null) {
            return null;
        }

        List<TriggerBehaviour<S, T>> actual = new ArrayList<>();
        for (TriggerBehaviour<S, T> triggerBehaviour : possible) {
            if (triggerBehaviour.isGuardConditionMet()) {
                actual.add(triggerBehaviour);
            }
        }

        if (actual.size() > 1) {
            throw new IllegalStateException("Multiple permitted exit transitions are configured from state '" + state + "' for trigger '" + trigger + "'. Guard clauses must be mutually exclusive.");
        }

        return actual.isEmpty() ? null : actual.get(0);
    }

    public void addEntryAction(final T trigger, final Action2<Transition<S, T>, Object[]> action) {
        assert action != null : "action is null";

        entryActions.add(new Action2<Transition<S, T>, Object[]>() {
            @Override
            public void doIt(Transition<S, T> t, Object[] args) {
                T trans_trigger = t.getTrigger();
                if (trans_trigger != null && trans_trigger.equals(trigger)) {
                    action.doIt(t, args);
                }
            }
        });
    }

    public void addEntryAction(Action2<Transition<S, T>, Object[]> action) {
        assert action != null : "action is null";
        entryActions.add(action);
    }

    public void insertEntryAction(Action2<Transition<S, T>, Object[]> action) {
        assert action != null : "action is null";
        entryActions.add(0, action);
    }

    public void addExitAction(Action2<Transition<S, T>, Object[]> action) {
        assert action != null : "action is null";
        exitActions.add(action);
    }

    public void initEnter(Transition<S, T> transition, Object... entryArgs) {
        assert transition != null : "transition is null";

        if (superstate != null) {
            superstate.initEnter(transition, entryArgs);
        }

        executeEntryActions(transition, entryArgs);
    }

    public void enter(Transition<S, T> transition, Object... entryArgs) {
        assert transition != null : "transition is null";

        if (transition.isReentry()) {
            executeEntryActions(transition, entryArgs);
        } else if (!includes(transition.getSource())) {
            if (superstate != null) {
                superstate.enter(transition, entryArgs);
            }

            executeEntryActions(transition, entryArgs);
        }
    }

    public void exit(Transition<S, T> transition,  Object... exitArgs) {
        assert transition != null : "transition is null";

        if (transition.isReentry()) {
            executeExitActions(transition, exitArgs);
        } else if (!includes(transition.getDestination())) {
            executeExitActions(transition, exitArgs);
            if (superstate != null) {
                superstate.exit(transition,exitArgs);
            }
        }
    }

    void executeEntryActions(Transition<S, T> transition, Object[] entryArgs) {
        assert transition != null : "transition is null";
        assert entryArgs != null : "entryArgs is null";
        for (Action2<Transition<S, T>, Object[]> action : entryActions) {
            action.doIt(transition, entryArgs);
        }
    }

    void executeExitActions(Transition<S, T> transition, Object[] exitArgs) {
        assert transition != null : "transition is null";
        assert exitArgs != null : "entryArgs is null";
        for (Action2<Transition<S, T>, Object[]> action : exitActions) {
            action.doIt(transition, exitArgs);
        }
    }

    public void addTriggerBehaviour(TriggerBehaviour<S, T> triggerBehaviour) {
        List<TriggerBehaviour<S, T>> allowed;
        if (!triggerBehaviours.containsKey(triggerBehaviour.getTrigger())) {
            allowed = new ArrayList<>();
            triggerBehaviours.put(triggerBehaviour.getTrigger(), allowed);
        }
        allowed = triggerBehaviours.get(triggerBehaviour.getTrigger());
        allowed.add(triggerBehaviour);
    }

    public StateRepresentation<S, T> getSuperstate() {
        return superstate;
    }

    public void setSuperstate(StateRepresentation<S, T> value) {
        superstate = value;
    }

    public S getUnderlyingState() {
        return state;
    }

    public void addSubstate(StateRepresentation<S, T> substate) {
        assert substate != null : "substate is null";
        substates.add(substate);
    }

    public boolean includes(S stateToCheck) {
        for (StateRepresentation<S, T> s : substates) {
            if (s.includes(stateToCheck)) {
                return true;
            }
        }
        return this.state.equals(stateToCheck);
    }

    public boolean isIncludedIn(S stateToCheck) {
        return this.state.equals(stateToCheck) || (superstate != null && superstate.isIncludedIn(stateToCheck));
    }

    @SuppressWarnings("unchecked")
    public List<T> getPermittedTriggers() {
        Set<T> result = new HashSet<>();

        for (T t : triggerBehaviours.keySet()) {
            for (TriggerBehaviour<S, T> v : triggerBehaviours.get(t)) {
                if (v.isGuardConditionMet()) {
                    result.add(t);
                    break;
                }
            }
        }

        if (getSuperstate() != null) {
            result.addAll(getSuperstate().getPermittedTriggers());
        }

        return new ArrayList<>(result);
    }

    public void addParallelStateMachineConfig(ParallelStateMachineConfig<S,T> stateMachineConfig) {
        this.parallelStates.add(stateMachineConfig);
    }

    public List<ParallelStateMachineConfig<S,T>> getParallelStateMachineConfigs() {
        return this.parallelStates;
    }

    public boolean isParallelState() {
        return !this.parallelStates.isEmpty();
    }

    public List<StateRepresentation<S,T>> getSubstates() {
        return substates;
    }
}
