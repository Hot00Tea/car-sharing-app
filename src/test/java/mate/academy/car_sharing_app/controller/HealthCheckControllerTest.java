package mate.academy.car_sharing_app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class HealthCheckControllerTest {

    private final HealthCheckController controller = new HealthCheckController();

    @Test
    void health_shouldReturnUpStatus() {
        assertEquals(Map.of("status", "UP"), controller.health());
    }
}
