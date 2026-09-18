package mate.academy.car_sharing_app.service;

import mate.academy.car_sharing_app.dto.carDto.CarRequestDto;
import mate.academy.car_sharing_app.dto.carDto.CarResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {

    CarResponseDto createCar(CarRequestDto carRequestDto);

    Page<CarResponseDto> getAll(Pageable pageable);

    CarResponseDto findById(Long id);

    CarResponseDto updateCar(Long id, CarRequestDto carRequestDto);

    void deleteById(Long id);
}
