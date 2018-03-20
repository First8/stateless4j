package com.github.oxo42.stateless4j.delegates;

@FunctionalInterface
public interface StateAccessor<S> {

	S call();
}
