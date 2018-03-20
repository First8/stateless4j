package com.github.oxo42.stateless4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import com.github.oxo42.stateless4j.delegates.Action;
import com.github.oxo42.stateless4j.transitions.Transition;

public class TransitionActionTests {

    private class TripwireAction implements Action<State, Trigger> {
        private boolean beenThere;

        public TripwireAction() {
            beenThere = false;
        }

        public boolean wasPerformed() {
            return beenThere;
        }

        @Override
        public void doIt(StateMachineContext<State, Trigger> context, Transition<State, Trigger> transition, Object... args) {
            beenThere = true;
        }
    }

    private class CountingAction implements Action<State, Trigger> {
        private List<Integer> numbers;
        private Integer number;

        public CountingAction(List<Integer> numbers, Integer number) {
            this.numbers = numbers;
            this.number = number;
        }

        @Override
        public void doIt(StateMachineContext<State, Trigger> context, Transition<State, Trigger> transition, Object... args) {
            numbers.add(this.number);
        }
    }

    @Test
    public void UnguardedActionIsPerformed() {
        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();

        TripwireAction action = new TripwireAction();

        config.configure(State.A)
                .permit(Trigger.Z, State.B, action);

        StateMachine<State, Trigger> sm = new StateMachine<>(State.A, config);
        sm.fire(Trigger.Z);

        assertEquals(State.B, sm.getStateMachineState().getState());
        assertTrue(action.wasPerformed());
    }

    @Test
    public void TransitionActionIsPerformedBetweenExitAndEntry() {
        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();

        List<Integer> list = new ArrayList<Integer>();
        Action<State, Trigger> exitAction = new CountingAction(list, new Integer(1));
        Action<State, Trigger> transitionAction = new CountingAction(list, new Integer(2));
        Action<State, Trigger> entryAction = new CountingAction(list, new Integer(3));

        config.configure(State.A)
                .onExit(exitAction)
                .permit(Trigger.Z, State.B, transitionAction);

        config.configure(State.B)
                .onEntry(entryAction);

        StateMachine<State, Trigger> sm = new StateMachine<>(State.A, config);
        sm.fire(Trigger.Z);

        assertEquals(State.B, sm.getStateMachineState().getState());

        assertEquals(3, list.size());
        assertEquals(new Integer(1), list.get(0));
        assertEquals(new Integer(2), list.get(1));
        assertEquals(new Integer(3), list.get(2));
    }

    @Test
    public void ActionWithPositiveGuardIsPerformed() {
        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();

        TripwireAction action = new TripwireAction();

        config.configure(State.A)
            .permitIf(Trigger.X, State.C, IgnoredTriggerBehaviourTests.returnTrue, action);

        StateMachine<State, Trigger> sm = new StateMachine<>(State.A, config);
        sm.fire(Trigger.X);

        assertEquals(State.C, sm.getStateMachineState().getState());
        assertTrue(action.wasPerformed());
    }

    @Test(expected = IllegalStateException.class)
    public void ActionWithNegativeGuardIsNotPerformed() {
        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();

        TripwireAction action = new TripwireAction();

        config.configure(State.A)
            .permitIf(Trigger.X, State.C, IgnoredTriggerBehaviourTests.returnFalse, action);

        StateMachine<State, Trigger> sm = new StateMachine<>(State.A, config);
        sm.fire(Trigger.X);
    }

    @Test
    public void ActionWithCorrectGuardIsPerformed() {
        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();

        TripwireAction correctAction = new TripwireAction();
        TripwireAction wrongAction = new TripwireAction();

        config.configure(State.A)
            .permitIf(Trigger.X, State.B, IgnoredTriggerBehaviourTests.returnFalse, wrongAction)
            .permitIf(Trigger.X, State.C, IgnoredTriggerBehaviourTests.returnTrue, correctAction);

        StateMachine<State, Trigger> sm = new StateMachine<>(State.A, config);
        sm.fire(Trigger.X);

        assertEquals(State.C, sm.getStateMachineState().getState());
        assertTrue(correctAction.wasPerformed());
        assertFalse(wrongAction.wasPerformed());
    }

    @Test
    public void UnguardedActionIsPerformedOnReentry() {
        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();

        TripwireAction action = new TripwireAction();

        config.configure(State.A)
                .permitReentry(Trigger.Z, action);

        StateMachine<State, Trigger> sm = new StateMachine<>(State.A, config);
        sm.fire(Trigger.Z);

        assertEquals(State.A, sm.getStateMachineState().getState());
        assertTrue(action.wasPerformed());
    }

    @Test
    public void ReentryActionIsPerformedBetweenExitAndEntry() {
        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();

        List<Integer> list = new ArrayList<Integer>();
        Action<State, Trigger> entryAction = new CountingAction(list, new Integer(3));
        Action<State, Trigger> transitionAction = new CountingAction(list, new Integer(2));
        Action<State, Trigger> exitAction = new CountingAction(list, new Integer(1));

        config.configure(State.A)
        		.onEntry(entryAction)
        		.onExit(exitAction)
                .permitReentry(Trigger.Z, transitionAction);

        StateMachine<State, Trigger> sm = new StateMachine<>(State.A, config);
        sm.fire(Trigger.Z);

        assertEquals(State.A, sm.getStateMachineState().getState());

        assertEquals(3, list.size());
        assertEquals(new Integer(1), list.get(0));
        assertEquals(new Integer(2), list.get(1));
        assertEquals(new Integer(3), list.get(2));
    }

    @Test
    public void ActionWithPositiveGuardIsPerformedOnReentry() {
        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();

        TripwireAction action = new TripwireAction();

        config.configure(State.A)
        	.permitReentryIf(Trigger.X, IgnoredTriggerBehaviourTests.returnTrue, action);

        StateMachine<State, Trigger> sm = new StateMachine<>(State.A, config);
        sm.fire(Trigger.X);

        assertEquals(State.A, sm.getStateMachineState().getState());
        assertTrue(action.wasPerformed());
    }
}
