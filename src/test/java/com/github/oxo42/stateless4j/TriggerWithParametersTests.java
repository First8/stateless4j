package com.github.oxo42.stateless4j;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import com.github.oxo42.stateless4j.triggers.ParameterizedTrigger;

public class TriggerWithParametersTests {

    @Test
    public void DescribesUnderlyingTrigger() {
        ParameterizedTrigger<State, Trigger> twp = new ParameterizedTrigger<>(Trigger.X, String.class);
        assertEquals(Trigger.X, twp.getTrigger());
    }

    @Test
    public void ParametersOfCorrectTypeAreAccepted() {
        ParameterizedTrigger<State, Trigger> twp = new ParameterizedTrigger<>(Trigger.X, String.class);
        twp.validateParameters(new Object[]{"arg"});
    }

    @Test
    public void ParametersArePolymorphic() {
        ParameterizedTrigger<State, Trigger> twp = new ParameterizedTrigger<>(Trigger.X, String.class);
        twp.validateParameters(new Object[]{"arg"});
    }

    @Test(expected = IllegalStateException.class)
    public void IncompatibleParametersAreNotValid() {
        ParameterizedTrigger<State, Trigger> twp = new ParameterizedTrigger<>(Trigger.X, String.class);
        twp.validateParameters(new Object[]{123});
    }

    @Test(expected = IllegalStateException.class)
    public void TooFewParametersDetected() {
        ParameterizedTrigger<State, Trigger> twp = new ParameterizedTrigger<>(Trigger.X, String.class, String.class);
        twp.validateParameters(new Object[]{"a"});
    }

    @Test(expected = IllegalStateException.class)
    public void TooManyParametersDetected() {
        ParameterizedTrigger<State, Trigger> twp = new ParameterizedTrigger<>(Trigger.X, String.class, String.class);
        twp.validateParameters(new Object[]{"a", "b", "c"});
    }
}
