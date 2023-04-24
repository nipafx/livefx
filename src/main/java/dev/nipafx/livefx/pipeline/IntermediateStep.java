package dev.nipafx.livefx.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

class IntermediateStep<T> implements Source<T>, Step<T> {

	private final List<Consumer<T>> nexts = new ArrayList<>();

	@Override
	public void emit(T item) {
		nexts.forEach(next -> next.accept(item));
	}

	@Override
	public Step<T> asStep() {
		return this;
	}

	@Override
	public <R> Step<R> then(Function<T, R> map) {
		var nextStep = new IntermediateStep<R>();
		nexts.add(item -> nextStep.emit(map.apply(item)));
		return nextStep;
	}

	@Override
	public <R, S extends T> Step<R> thenIf(Class<S> token, Function<S, R> map) {
		var nextStep = new IntermediateStep<R>();
		nexts.add(item -> {
			if (token.isInstance(item))
				nextStep.emit(map.apply(token.cast(item)));
		});
		return nextStep;
	}

	@Override
	public Step<T> sink(Consumer<T> consume) {
		nexts.add(consume);
		return this;
	}

	@Override
	public <S extends T> Step<T> sinkIf(Class<S> token, Consumer<S> consume) {
		nexts.add(item -> {
			if (token.isInstance(item))
				consume.accept(token.cast(item));
		});
		return this;
	}

}
