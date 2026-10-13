package mate.academy.car_sharing_app.service.carService;

import mate.academy.car_sharing_app.dto.carDto.CarRequestDto;
import mate.academy.car_sharing_app.dto.carDto.CarResponseDto;
import mate.academy.car_sharing_app.exception.CarException;
import mate.academy.car_sharing_app.mapper.CarMapper;
import mate.academy.car_sharing_app.model.car.Car;
import mate.academy.car_sharing_app.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {

    private static final Long CAR_ID = 1L;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarServiceImpl carService;

    private Car car;
    private CarRequestDto requestDto;
    private CarResponseDto responseDto;

    @BeforeEach
    void setUp() {
        car = new Car();
        car.setId(CAR_ID);
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setInventory(5);
        car.setDailyFee(BigDecimal.valueOf(50));

        requestDto = new CarRequestDto();
        requestDto.setBrand("Toyota");
        requestDto.setModel("Camry");
        requestDto.setInventory(5);
        requestDto.setDailyFee(BigDecimal.valueOf(50));

        responseDto = new CarResponseDto();
    }

    @Test
    void createCar_shouldSaveCarAndReturnDto() {
        when(carMapper.toEntity(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(responseDto);

        CarResponseDto result = carService.createCar(requestDto);

        assertNotNull(result);
        verify(carMapper).toEntity(requestDto);
        verify(carRepository).save(car);
        verify(carMapper).toDto(car);
    }

    @Test
    void getAll_shouldReturnMappedCars() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> cars = new PageImpl<>(List.of(car));

        when(carRepository.findAll(pageable)).thenReturn(cars);
        when(carMapper.toDto(car)).thenReturn(responseDto);

        Page<CarResponseDto> result = carService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(responseDto, result.getContent().get(0));
        verify(carRepository).findAll(pageable);
        verify(carMapper).toDto(car);
    }

    @Test
    void findById_shouldReturnCarDto() {
        when(carRepository.findById(CAR_ID)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(responseDto);

        CarResponseDto result = carService.findById(CAR_ID);

        assertNotNull(result);
        verify(carMapper).toDto(car);
    }

    @Test
    void findById_shouldThrowWhenCarDoesNotExist() {
        when(carRepository.findById(CAR_ID)).thenReturn(Optional.empty());

        assertThrows(CarException.class, () -> carService.findById(CAR_ID));

        verify(carMapper, never()).toDto(car);
    }

    @Test
    void updateCar_shouldUpdateCarFields() {
        requestDto.setBrand("BMW");
        requestDto.setModel("X5");
        requestDto.setInventory(3);
        requestDto.setDailyFee(BigDecimal.valueOf(100));

        when(carRepository.findById(CAR_ID)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(responseDto);

        CarResponseDto result = carService.updateCar(CAR_ID, requestDto);

        assertNotNull(result);
        assertEquals("BMW", car.getBrand());
        assertEquals("X5", car.getModel());
        assertEquals(3, car.getInventory());
        assertEquals(BigDecimal.valueOf(100), car.getDailyFee());

        verify(carMapper).toDto(car);
    }

    @Test
    void updateCar_shouldThrowWhenCarDoesNotExist() {
        when(carRepository.findById(CAR_ID)).thenReturn(Optional.empty());

        assertThrows(
                CarException.class,
                () -> carService.updateCar(CAR_ID, requestDto)
        );

        verify(carMapper, never()).toDto(car);
    }

    @Test
    void deleteById_shouldDeleteExistingCar() {
        when(carRepository.findById(CAR_ID)).thenReturn(Optional.of(car));

        carService.deleteById(CAR_ID);

        verify(carRepository).delete(car);
    }

    @Test
    void deleteById_shouldThrowWhenCarDoesNotExist() {
        when(carRepository.findById(CAR_ID)).thenReturn(Optional.empty());

        assertThrows(
                CarException.class,
                () -> carService.deleteById(CAR_ID)
        );

        verify(carRepository, never()).delete(car);
    }
}
