package com.github.oxo42.stateless4j.triggers;

import com.github.oxo42.stateless4j.OutVar;
import com.github.oxo42.stateless4j.StateMachineContext;
import com.github.oxo42.stateless4j.delegates.Action;
import com.github.oxo42.stateless4j.delegates.Func;

public class DynamicTriggerBehaviour<S, T> extends TriggerBehaviour<S, T> {

    private final Func<S,S,T> destination;
    private final Action<S,T> action;

    public DynamicTriggerBehaviour(T trigger, Func<S,S,T> destination, Func<Boolean,S,T> guard, Action<S,T> action) {
        super(trigger, guard);
        assert destination != null : "destination is null";
        this.destination = destination;
        this.action = action;
    }
    
    @Override
    public void performAction(StateMachineContext<S,T> context, Object[] args) {
        // TODO check if transition should be null
        action.doIt(context, null, args);
    }

    @Override
    public boolean resultsInTransitionFrom(StateMachineContext<S,T> context, S source, Object[] args, OutVar<S> dest) {
        dest.set(destination.call(context,args));
        return true;
    }
}
