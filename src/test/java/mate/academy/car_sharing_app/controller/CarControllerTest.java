package mate.academy.car_sharing_app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import mate.academy.car_sharing_app.dto.carDto.CarRequestDto;
import mate.academy.car_sharing_app.dto.carDto.CarResponseDto;
import mate.academy.car_sharing_app.service.carService.CarService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CarControllerTest {

    @Mock
    private CarService carService;

    @InjectMocks
    private CarController carController;

    @Test
    void findAll_shouldReturnPageOfCars() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CarResponseDto> expectedPage = new PageImpl<>(java.util.List.of());

        when(carService.getAll(pageable)).thenReturn(expectedPage);

        Page<CarResponseDto> actualPage = carController.findAll(pageable);

        assertEquals(expectedPage, actualPage);
        verify(carService).getAll(pageable);
    }

    @Test
    void findById_shouldReturnCar() {
        Long carId = 1L;
        CarResponseDto expectedDto = new CarResponseDto();

        when(carService.findById(carId)).thenReturn(expectedDto);

        CarResponseDto actualDto = carController.findById(carId);

        assertEquals(expectedDto, actualDto);
        verify(carService).findById(carId);
    }

    @Test
    void createCar_shouldReturnCreatedCar() {
        CarRequestDto requestDto = new CarRequestDto();
        CarResponseDto expectedDto = new CarResponseDto();

        when(carService.createCar(requestDto)).thenReturn(expectedDto);

        CarResponseDto actualDto = carController.createCar(requestDto);

        assertEquals(expectedDto, actualDto);
        verify(carService).createCar(requestDto);
    }

    @Test
    void updateCar_shouldReturnUpdatedCar() {
        Long carId = 1L;
        CarRequestDto requestDto = new CarRequestDto();
        CarResponseDto expectedDto = new CarResponseDto();

        when(carService.updateCar(carId, requestDto)).thenReturn(expectedDto);

        CarResponseDto actualDto = carController.updateCar(carId, requestDto);

        assertEquals(expectedDto, actualDto);
        verify(carService).updateCar(carId, requestDto);
    }

    @Test
    void delete_shouldCallService() {
        Long carId = 1L;

        carController.delete(carId);

        verify(carService).deleteById(carId);
    }
}
