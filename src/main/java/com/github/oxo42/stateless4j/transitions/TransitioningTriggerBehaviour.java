package com.github.oxo42.stateless4j.transitions;

import com.github.oxo42.stateless4j.OutVar;
import com.github.oxo42.stateless4j.StateMachineContext;
import com.github.oxo42.stateless4j.delegates.Action;
import com.github.oxo42.stateless4j.delegates.Func;
import com.github.oxo42.stateless4j.triggers.TriggerBehaviour;

public class TransitioningTriggerBehaviour<S, T> extends TriggerBehaviour<S, T> {

    private final S destination;
    private final Action<S,T> action;

    public TransitioningTriggerBehaviour(T trigger, S destination, Func<Boolean,S,T> guard, Action<S,T> action) {
        super(trigger, guard);
        this.destination = destination;
        this.action = action;
    }
    
    @Override
    public void performAction(StateMachineContext<S,T> context, Object[] args) {
        action.doIt(context,null,args);
    }

    @Override
    public boolean resultsInTransitionFrom(StateMachineContext<S,T> context, S source, Object[] args, OutVar<S> dest) {
        dest.set(destination);
        return true;
    }

    public S getDestination() {
        return destination;
    }
}
