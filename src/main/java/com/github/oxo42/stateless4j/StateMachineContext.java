package com.github.oxo42.stateless4j;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class StateMachineContext<S, T> {

	private final Map<String,Object> attributes = new HashMap<>();

    private ContextedStateMachine<S, T> contextedStateMachine;

    public StateMachineContext(ContextedStateMachine<S, T> contextedStateMachine) {
        this.contextedStateMachine = contextedStateMachine;
    }

    public Map<String, Object> getAttributes() {
		return attributes;
	}

	public Object getAttribute(String name) {
		return this.attributes.get(name);
	}

	public Object setAttribute(String name, Object value) {
		return this.attributes.put(name,value);
	}

	public Stream<Map.Entry<String,Object>> attributes() {
		return this.attributes.entrySet().stream();
	}

    public ContextedStateMachine<S, T> getContextedStateMachine() {
        return contextedStateMachine;
    }
}
