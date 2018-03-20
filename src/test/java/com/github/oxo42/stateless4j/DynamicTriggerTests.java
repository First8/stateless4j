package com.github.oxo42.stateless4j;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import com.github.oxo42.stateless4j.triggers.ParameterizedTrigger;

public class DynamicTriggerTests {

    @Test
    public void DestinationStateIsDynamic() {
        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();
        config.configure(State.A).permitDynamic(Trigger.X, (context, args) -> State.B);

        StateMachine<State, Trigger> sm = new StateMachine<>(State.A, config);
        sm.fire(Trigger.X);

        assertEquals(State.B, sm.getStateMachineState().getState());
    }

    @Test
    public void DestinationStateIsCalculatedBasedOnTriggerParameters() {
        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();
        ParameterizedTrigger<State, Trigger> trigger = config.setTriggerParameters(
                Trigger.X, Integer.class);
        config.configure(State.A).permitDynamic(trigger, ((context, args) -> ((Integer) args[0]) == 1 ? State.B : State.C));

        StateMachine<State, Trigger> sm = new StateMachine<>(State.A, config);
        sm.fire(Trigger.X, 1);

        assertEquals(State.B, sm.getStateMachineState().getState());
    }
}
