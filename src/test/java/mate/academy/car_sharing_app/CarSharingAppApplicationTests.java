package mate.academy.car_sharing_app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class CarSharingAppApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Container
	@ServiceConnection
	static MySQLContainer<?> mysql =
			new MySQLContainer<>("mysql:8.4");

	@Test
	void contextLoads() {
	}

	@Test
	void getCars_shouldBeAvailableWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/cars"))
				.andExpect(status().isOk());
	}
}
