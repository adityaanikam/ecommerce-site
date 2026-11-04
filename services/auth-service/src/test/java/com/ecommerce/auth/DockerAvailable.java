package com.ecommerce.auth;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Skips a test instead of failing it when there is no Docker daemon to run
 * Testcontainers against.
 *
 * This is deliberate rather than lazy: the integration tests here need real MongoDB
 * and real Kafka, and a machine without Docker genuinely cannot run them. Skipping
 * says so out loud in the surefire report, where a hard failure would just look like
 * a broken build. The unit and slice tests cover the same logic without Docker.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DockerAvailable.Condition.class)
public @interface DockerAvailable {

    class Condition implements ExecutionCondition {

        private static volatile Boolean available;

        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            if (available == null) {
                synchronized (Condition.class) {
                    if (available == null) {
                        try {
                            available = DockerClientFactory.instance().isDockerAvailable();
                        } catch (RuntimeException e) {
                            available = false;
                        }
                    }
                }
            }
            return available
                    ? ConditionEvaluationResult.enabled("Docker is available")
                    : ConditionEvaluationResult.disabled(
                            "SKIPPED: no Docker daemon, Testcontainers integration test cannot run");
        }
    }
}
