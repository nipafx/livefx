package dev.nipafx.calendar.spring;

import dev.nipafx.calendar.data.RepositoryConfigurationException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

public class RepositoryFailureAnalyzer extends AbstractFailureAnalyzer<RepositoryConfigurationException> {

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, RepositoryConfigurationException cause) {
		return new FailureAnalysis(cause.getDescription(), cause.getAction(), cause);
	}

}
