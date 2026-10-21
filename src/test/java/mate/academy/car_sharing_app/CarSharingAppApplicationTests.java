package mate.academy.car_sharing_app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CarSharingAppApplicationTests {

	@LocalServerPort
	private int port;

	@Container
	@ServiceConnection
	static MySQLContainer<?> mysql =
			new MySQLContainer<>("mysql:8.4");

	@Test
	void contextLoads() {
	}

	@Test
	void getCars_shouldBeAvailableWithoutAuthentication()
			throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:%d/cars".formatted(port)))
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

		assertEquals(200, response.statusCode());
	}
}
