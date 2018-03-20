package com.github.oxo42.stateless4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import com.github.oxo42.stateless4j.delegates.Action;
import com.github.oxo42.stateless4j.delegates.Func;
import com.github.oxo42.stateless4j.triggers.IgnoredTriggerBehaviour;

public class IgnoredTriggerBehaviourTests {

    public static Func<Boolean, State, Trigger> returnTrue = (context, args) -> true;

    public static Func<Boolean, State, Trigger> returnFalse = (context, args) -> false;

    public static Action<State, Trigger> nopAction = (context, transition, args) -> { };

    @Test
    public void StateRemainsUnchanged() {
        IgnoredTriggerBehaviour<State, Trigger> ignored = new IgnoredTriggerBehaviour<>(Trigger.X, returnTrue);
        assertFalse(ignored.resultsInTransitionFrom(null, State.B, new Object[0], new OutVar<>()));
    }

    @Test
    public void ExposesCorrectUnderlyingTrigger() {
        IgnoredTriggerBehaviour<State, Trigger> ignored = new IgnoredTriggerBehaviour<>(Trigger.X, returnTrue);
        assertEquals(Trigger.X, ignored.getTrigger());
    }

    @Test
    public void WhenGuardConditionFalse_IsGuardConditionMetIsFalse() {
        IgnoredTriggerBehaviour<State, Trigger> ignored = new IgnoredTriggerBehaviour<>(Trigger.X, returnFalse);
        assertFalse(ignored.isGuardConditionMet(null, null));
    }

    @Test
    public void WhenGuardConditionTrue_IsGuardConditionMetIsTrue() {
        IgnoredTriggerBehaviour<State, Trigger> ignored = new IgnoredTriggerBehaviour<>(Trigger.X, returnTrue);
        assertTrue(ignored.isGuardConditionMet(null, null));
    }
}
