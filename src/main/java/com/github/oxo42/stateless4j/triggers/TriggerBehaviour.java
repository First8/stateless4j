package com.github.oxo42.stateless4j.triggers;

import com.github.oxo42.stateless4j.OutVar;
import com.github.oxo42.stateless4j.StateMachineContext;
import com.github.oxo42.stateless4j.delegates.Func;

public abstract class TriggerBehaviour<S, T> {

    private final T trigger;
    private final Func<Boolean,S,T> guard;

    protected TriggerBehaviour(T trigger, Func<Boolean,S,T> guard) {
        this.trigger = trigger;
        this.guard = guard;
    }

    public T getTrigger() {
        return trigger;
    }

    public abstract void performAction(StateMachineContext<S,T> context, Object[] args);

    public boolean isGuardConditionMet(StateMachineContext<S,T> context, Object[] args) {
        return guard.call(context,args);
    }

    public abstract boolean resultsInTransitionFrom(StateMachineContext<S,T> context, S source, Object[] args, OutVar<S> dest);
}
