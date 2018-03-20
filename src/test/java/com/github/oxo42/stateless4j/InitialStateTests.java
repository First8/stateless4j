package com.github.oxo42.stateless4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import com.github.oxo42.stateless4j.triggers.ParameterizedTrigger;

public class InitialStateTests {

    private boolean executed = false;

    @Test
    public void testInitialStateEntryActionNotExecuted() {
        final State initial = State.B;

        StateMachineConfig<State, Trigger> config = config(initial);

        StateMachine<State, Trigger> sm = new StateMachine<>(initial, config);
        assertEquals(initial, sm.getStateMachineState().getState());
        assertFalse(executed);
    }

    @Test
    public void testInitialStateEntryActionExecuted() {
        final State initial = State.B;

        StateMachineConfig<State, Trigger> config = config(initial);
        config.enableEntryActionOfInitialState();

        StateMachine<State, Trigger> sm = new StateMachine<>(initial, config);
        assertEquals(initial, sm.getStateMachineState().getState());
        assertTrue(executed);
    }

    private StateMachineConfig<State, Trigger> config(final State initial) {
        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();
        config.configure(initial)
                .onEntry((context, transition, args) -> executed = true);
        return config;
    }

    @Test
    public void testInitialStateEntryActionWithParameterNotExecuted() {
        final State initial = State.B;

        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();

        ParameterizedTrigger<State, Trigger> trigger = config.setTriggerParameters(Trigger.X, Object.class);

        config.configure(initial).onEntryFrom(trigger, (context, transition, args) -> executed = true);

        config.enableEntryActionOfInitialState();

        StateMachine<State, Trigger> sm = new StateMachine<>(initial, config);
        assertEquals(initial, sm.getStateMachineState().getState());
        assertFalse(executed);
    }
}
