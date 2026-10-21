package mate.academy.car_sharing_app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CarSharingAppApplicationTests {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Container
	@ServiceConnection
	static MySQLContainer<?> mysql =
			new MySQLContainer<>("mysql:8.4");

	@Test
	void contextLoads() {
	}

	@Test
	void getCars_shouldBeAvailableWithoutAuthentication() {
		ResponseEntity<String> response = testRestTemplate.getForEntity(
				"/cars",
				String.class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
}
