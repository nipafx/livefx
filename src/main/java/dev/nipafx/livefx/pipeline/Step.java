package dev.nipafx.livefx.pipeline;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Step<T> {

	<R> Step<R> then(Function<T, R> map);
	<R, S extends T> Step<R> thenIf(Class<S> token, Function<S, R> map);

	Step<T> sink(Consumer<T> consume);
	<S extends T> Step<T> sinkIf(Class<S> token, Consumer<S> map);

}
