package org.springframework.samples.petclinic.vet.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Observability and Tracing.
 */
@Configuration
public class ObservabilityConfig {

    /**
     * Enable the @Observed annotation for manual tracing of methods.
     */
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}
