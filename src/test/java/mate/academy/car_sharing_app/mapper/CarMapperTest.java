package mate.academy.car_sharing_app.mapper;

import java.math.BigDecimal;
import mate.academy.car_sharing_app.dto.carDto.CarRequestDto;
import mate.academy.car_sharing_app.dto.carDto.CarResponseDto;
import mate.academy.car_sharing_app.model.car.Car;
import mate.academy.car_sharing_app.model.car.CarType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CarMapperTest {

    private CarMapper carMapper;

    @BeforeEach
    void setUp() {
        carMapper = Mappers.getMapper(CarMapper.class);
    }

    @Test
    void toEntity_shouldMapFieldsAndIgnoreId() {
        CarRequestDto requestDto = new CarRequestDto();
        requestDto.setBrand("Toyota");
        requestDto.setModel("Camry");
        requestDto.setType(CarType.SEDAN);
        requestDto.setInventory(5);
        requestDto.setDailyFee(BigDecimal.valueOf(50));

        Car car = carMapper.toEntity(requestDto);

        assertNull(car.getId());
        assertEquals("Toyota", car.getBrand());
        assertEquals("Camry", car.getModel());
        assertEquals(CarType.SEDAN, car.getType());
        assertEquals(5, car.getInventory());
        assertEquals(BigDecimal.valueOf(50), car.getDailyFee());
    }

    @Test
    void toDto_shouldMapAllFields() {
        Car car = new Car();
        car.setId(1L);
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setType(CarType.SEDAN);
        car.setInventory(5);
        car.setDailyFee(BigDecimal.valueOf(50));

        CarResponseDto responseDto = carMapper.toDto(car);

        assertEquals(1L, responseDto.getId());
        assertEquals("Toyota", responseDto.getBrand());
        assertEquals("Camry", responseDto.getModel());
        assertEquals(CarType.SEDAN, responseDto.getType());
        assertEquals(5, responseDto.getInventory());
        assertEquals(BigDecimal.valueOf(50), responseDto.getDailyFee());
    }
}
