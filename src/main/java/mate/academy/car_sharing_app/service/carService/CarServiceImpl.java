package mate.academy.car_sharing_app.service.carService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mate.academy.car_sharing_app.dto.carDto.CarRequestDto;
import mate.academy.car_sharing_app.dto.carDto.CarResponseDto;
import mate.academy.car_sharing_app.exception.EntityNotFoundException;
import mate.academy.car_sharing_app.mapper.CarMapper;
import mate.academy.car_sharing_app.model.car.Car;
import mate.academy.car_sharing_app.repository.CarRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public CarResponseDto createCar(CarRequestDto carRequestDto) {
        Car car = carMapper.toEntity(carRequestDto);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    public Page<CarResponseDto> getAll(Pageable pageable) {
        return carRepository.findAll(pageable)
                .map(carMapper::toDto);
    }

    @Override
    public CarResponseDto findById(Long id) {
        Car car = getCarById(id);
        return carMapper.toDto(car);
    }

    @Override
    public CarResponseDto updateCar(Long id, CarRequestDto carRequestDto) {
        Car car = getCarById(id);
        carMapper.updateFromDto(carRequestDto, car);

        return carMapper.toDto(car);
    }

    @Override
    public void deleteById(Long id) {
        Car car = getCarById(id);
        carRepository.delete(car);
    }

    private Car getCarById(Long id) {
        return carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Car", "id", id)
        );
    }
}
