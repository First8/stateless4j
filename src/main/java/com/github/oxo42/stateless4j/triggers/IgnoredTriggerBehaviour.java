package com.github.oxo42.stateless4j.triggers;

import com.github.oxo42.stateless4j.OutVar;
import com.github.oxo42.stateless4j.StateMachineContext;
import com.github.oxo42.stateless4j.delegates.Func;

public class IgnoredTriggerBehaviour<S, T> extends TriggerBehaviour<S, T> {

    public IgnoredTriggerBehaviour(T trigger, Func<Boolean, S, T> guard) {
        super(trigger, guard);
    }
    
    @Override
    public void performAction(StateMachineContext<S, T> context, Object[] args) {
        // no need to do anything. This is never called (no transition => no action)
    }

    @Override
    public boolean resultsInTransitionFrom(StateMachineContext<S,T> context, S source, Object[] args, OutVar<S> dest) {
        return false;
    }
}
