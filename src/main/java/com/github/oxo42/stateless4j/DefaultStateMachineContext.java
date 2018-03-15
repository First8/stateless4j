package com.github.oxo42.stateless4j;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Default implementation of {@link StateMachineContext}.
 *
 * {@inheritDoc}
 */
public class DefaultStateMachineContext<S, T> implements StateMachineContext<S, T> {

	private final Map<String,Object> attributes = new HashMap<>();

    private StateMachine<S, T> stateMachine;

    @Override
    public void setStateMachine(StateMachine<S, T> contextedStateMachine) {
        this.stateMachine = contextedStateMachine;
    }

    @Override
    public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
    public Object getAttribute(String key) {
		return this.attributes.get(key);
	}

	@Override
    public Object setAttribute(String key, Object value) {
		return this.attributes.put(key,value);
	}

	@Override
    public Stream<Map.Entry<String,Object>> attributes() {
		return this.attributes.entrySet().stream();
	}

    @Override
    public StateMachine<S, T> getTopLevelStateMachine() {
        return stateMachine;
    }

    @Override
	public Object getAttribute(String name, Object initialValue) {
    	attributes.putIfAbsent(name,initialValue);
    	return attributes.get(name);
	}
}
