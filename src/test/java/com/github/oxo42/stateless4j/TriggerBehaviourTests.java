package com.github.oxo42.stateless4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import com.github.oxo42.stateless4j.transitions.TransitioningTriggerBehaviour;

public class TriggerBehaviourTests {

    @Test
    public void ExposesCorrectUnderlyingTrigger() {
        TransitioningTriggerBehaviour<State, Trigger> transtioning = new TransitioningTriggerBehaviour<>(
                Trigger.X, State.C, IgnoredTriggerBehaviourTests.returnTrue, IgnoredTriggerBehaviourTests.nopAction);

        assertEquals(Trigger.X, transtioning.getTrigger());
    }

    @Test
    public void WhenGuardConditionFalse_IsGuardConditionMetIsFalse() {
        TransitioningTriggerBehaviour<State, Trigger> transtioning = new TransitioningTriggerBehaviour<>(
                Trigger.X, State.C, IgnoredTriggerBehaviourTests.returnFalse, IgnoredTriggerBehaviourTests.nopAction);

        assertFalse(transtioning.isGuardConditionMet(null, null));
    }

    @Test
    public void WhenGuardConditionTrue_IsGuardConditionMetIsTrue() {
        TransitioningTriggerBehaviour<State, Trigger> transtioning = new TransitioningTriggerBehaviour<>(
                Trigger.X, State.C, IgnoredTriggerBehaviourTests.returnTrue, IgnoredTriggerBehaviourTests.nopAction);

        assertTrue(transtioning.isGuardConditionMet(null, null));
    }
}
