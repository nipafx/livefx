package dev.nipafx.livefx.pipeline;

public interface Source<T> {

	void emit(T item);

	Step<T> asStep();

	static <T> Source<T> create() {
		return new IntermediateStep<T>();
	}

}
