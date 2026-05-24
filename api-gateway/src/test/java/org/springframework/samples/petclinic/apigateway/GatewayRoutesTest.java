package org.springframework.samples.petclinic.apigateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for API Gateway route configuration.
 *
 * <p>Verifies that all three service routes are correctly defined with
 * {@code lb://} (LoadBalancer) URIs as required by the TO-BE architecture.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
@DisplayName("API Gateway — Route Configuration Tests")
class GatewayRoutesTest {

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    private List<RouteDefinition> routes;

    @BeforeEach
    void setUp() {
        routes = routeDefinitionLocator.getRouteDefinitions().collectList().block();
        assertThat(routes).isNotNull();
    }

    // ─── Context ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("application context should load without errors")
    void applicationContext_shouldLoadSuccessfully() {
        assertThat(routeDefinitionLocator).isNotNull();
    }

    // ─── Route count ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("should have exactly three routes defined (customer, vet, visit)")
    void shouldHaveExactlyThreeRoutesDefined() {
        assertThat(routes).hasSize(3);
    }

    // ─── customer-service route ───────────────────────────────────────────────

    @Test
    @DisplayName("customer-service route should exist with correct id")
    void customerServiceRoute_shouldExistWithCorrectId() {
        assertThat(routes)
                .extracting(RouteDefinition::getId)
                .contains("customer-service");
    }

    @Test
    @DisplayName("customer-service route should use lb://customer-service URI")
    void customerServiceRoute_shouldUseLbUri() {
        RouteDefinition route = findRouteById("customer-service");
        assertThat(route.getUri().getScheme()).isEqualTo("lb");
        assertThat(route.getUri().getHost()).isEqualTo("customer-service");
    }

    // ─── vet-service route ────────────────────────────────────────────────────

    @Test
    @DisplayName("vet-service route should use lb://vet-service URI")
    void vetServiceRoute_shouldUseLbUri() {
        RouteDefinition route = findRouteById("vet-service");
        assertThat(route.getUri().getScheme()).isEqualTo("lb");
        assertThat(route.getUri().getHost()).isEqualTo("vet-service");
    }

    // ─── visit-service route ──────────────────────────────────────────────────

    @Test
    @DisplayName("visit-service route should use lb://visit-service URI")
    void visitServiceRoute_shouldUseLbUri() {
        RouteDefinition route = findRouteById("visit-service");
        assertThat(route.getUri().getScheme()).isEqualTo("lb");
        assertThat(route.getUri().getHost()).isEqualTo("visit-service");
    }

    // ─── Cross-cutting ────────────────────────────────────────────────────────

    @Test
    @DisplayName("all routes should use lb:// scheme (LoadBalancer pattern)")
    void allRoutes_shouldUseLoadBalancerScheme() {
        assertThat(routes)
                .allSatisfy(route ->
                        assertThat(route.getUri().getScheme())
                                .as("Route '%s' must use lb:// URI", route.getId())
                                .isEqualTo("lb"));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private RouteDefinition findRouteById(String id) {
        return routes.stream()
                .filter(r -> id.equals(r.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Route not found: " + id));
    }
}
