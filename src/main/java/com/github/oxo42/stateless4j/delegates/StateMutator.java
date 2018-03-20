package com.github.oxo42.stateless4j.delegates;

@FunctionalInterface
public interface StateMutator<S> {

	void accept(S state);
}
