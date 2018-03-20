package com.github.oxo42.stateless4j;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class DynamicTransitionActionTests {

    @Test
    public void UnguardedDynamicTransitionActionsArePerformed() {
        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();

        List<Integer> actionsPerformed = new ArrayList<>();

        config.configure(State.A)
                .permitDynamic(Trigger.X, (context, args) -> State.B, ((context, transition, args) -> actionsPerformed.add(1)));

        StateMachine<State, Trigger> sm = new StateMachine<>(State.A, config);
        sm.fire(Trigger.X);

        assertEquals(State.B, sm.getStateMachineState().getState());
        assertEquals(1, actionsPerformed.size());
        assertEquals(new Integer(1), actionsPerformed.get(0));
    }

    @Test
    public void GuardedDynamicTransitionActionsArePerformed() {
        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();

        List<Integer> actionsPerformed = new ArrayList<>();

        config.configure(State.A)
                .permitDynamicIf(Trigger.X,
                        (context, args) -> State.B, ((context, args) -> ((Integer) args[0]) == 1),
                        ((context, transition, args) -> actionsPerformed.add(1)));

        StateMachine<State, Trigger> sm = new StateMachine<>(State.A, config);
        sm.fire(Trigger.X, 1);

        assertEquals(State.B, sm.getStateMachineState().getState());
        assertEquals(1, actionsPerformed.size());
        assertEquals(new Integer(1), actionsPerformed.get(0));
    }
}
