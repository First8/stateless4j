package com.github.oxo42.stateless4j.triggers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.oxo42.stateless4j.OutVar;
import com.github.oxo42.stateless4j.StateMachineContext;
import com.github.oxo42.stateless4j.delegates.Func;

public abstract class TriggerBehaviour<S, T> {

    private final T trigger;
    private final Func<Boolean,S,T> guard;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected TriggerBehaviour(T trigger, Func<Boolean,S,T> guard) {
        this.trigger = trigger;
        this.guard = guard;
    }

    public T getTrigger() {
        return trigger;
    }

    public abstract void performAction(StateMachineContext<S,T> context, Object[] args);

    public boolean isGuardConditionMet(StateMachineContext<S,T> context, Object[] args) {
        try {
            return guard.call(context, args);
        } catch (Exception e) {
            logger.warn("Failed to evaluate guard condition: {}, assuming false.", e.getMessage());
            logger.trace("Failed to evaluate guard condition: {}, assuming false.", e.getMessage(), e);
            return false;
        }
    }

    public abstract boolean resultsInTransitionFrom(StateMachineContext<S,T> context, S source, Object[] args, OutVar<S> dest);
}
