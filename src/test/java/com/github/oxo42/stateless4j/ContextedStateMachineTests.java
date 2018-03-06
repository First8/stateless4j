package com.github.oxo42.stateless4j;

import org.junit.Assert;
import org.junit.Test;

public class ContextedStateMachineTests {

    @Test
    public void OnEntrySuccessfullyUpdatesContext() {
        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();

        config.configure(State.A)
                .permit(Trigger.X, State.B);

        config.configure(State.B)
                .onEntry(((arg1, arg2) -> arg1.setAttribute("test-key", "123")), DefaultStateMachineContext.class);

        ContextedStateMachine<State, Trigger> contextedStateMachine = new ContextedStateMachine<>(State.A, config);

        Assert.assertEquals(null, contextedStateMachine.getStateMachineContext().getAttribute("test-key"));

        contextedStateMachine.fire(Trigger.X, contextedStateMachine.getStateMachineContext());

        Assert.assertEquals(1, contextedStateMachine.getStateMachineContext().attributes().count());
        Assert.assertEquals("123", contextedStateMachine.getStateMachineContext().getAttribute("test-key"));
    }

    @Test
    public void OnEntrySkippedDueToMissingArgument() {
        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();

        config.configure(State.A)
                .permit(Trigger.X, State.B);

        config.configure(State.B)
                .onEntry(((arg1, arg2) -> arg1.setAttribute("test-key", "123")), DefaultStateMachineContext.class);

        ContextedStateMachine<State, Trigger> contextedStateMachine = new ContextedStateMachine<>(State.A, config);

        contextedStateMachine.fire(Trigger.X);

        Assert.assertEquals(null, contextedStateMachine.getStateMachineContext().getAttribute("test-key"));
    }

    @Test
    public void OnExitSuccessfullyUpdatesContext() {
        StateMachineConfig<State, Trigger> config = new StateMachineConfig<>();

        config.configure(State.A)
                .permit(Trigger.X, State.B);

        config.configure(State.A)
                .onExit(((arg1, arg2) -> arg1.setAttribute("test-key", "123")), DefaultStateMachineContext.class);

        ContextedStateMachine<State, Trigger> contextedStateMachine = new ContextedStateMachine<>(State.A, config);

        Assert.assertEquals(null, contextedStateMachine.getStateMachineContext().getAttribute("test-key"));

        contextedStateMachine.fire(Trigger.X, contextedStateMachine.getStateMachineContext());

        Assert.assertEquals(1, contextedStateMachine.getStateMachineContext().attributes().count());
        Assert.assertEquals("123", contextedStateMachine.getStateMachineContext().getAttribute("test-key"));
    }
}
