package com.github.oxo42.stateless4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import com.github.oxo42.stateless4j.delegates.Action;

public class TransitionActionTests {

    final Enum StateA = State.A, StateB = State.B, StateC = State.C,
            TriggerX = Trigger.X, TriggerY = Trigger.Y;

    private class TripwireAction implements Action {
        private boolean beenThere;

        public TripwireAction() {
            beenThere = false;
        }

        public boolean wasPerformed() {
            return beenThere;
        }

        @Override
        public void doIt() {
            beenThere = true;
        }
    }

    private class CountingAction implements Action {
        private List<Integer> numbers;
        private Integer number;

        public CountingAction(List<Integer> numbers, Integer number) {
            this.numbers = numbers;
            this.number = number;
        }

        @Override
        public void doIt() {
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
        Action exitAction = new CountingAction(list, new Integer(1));
        Action transitionAction = new CountingAction(list, new Integer(2));
        Action entryAction = new CountingAction(list, new Integer(3));

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
        Action entryAction = new CountingAction(list, new Integer(3));
        Action transitionAction = new CountingAction(list, new Integer(2));
        Action exitAction = new CountingAction(list, new Integer(1));

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
