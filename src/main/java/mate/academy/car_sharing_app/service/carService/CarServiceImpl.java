package mate.academy.car_sharing_app.service.carService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mate.academy.car_sharing_app.dto.carDto.CarRequestDto;
import mate.academy.car_sharing_app.dto.carDto.CarResponseDto;
import mate.academy.car_sharing_app.exception.CarException;
import mate.academy.car_sharing_app.mapper.CarMapper;
import mate.academy.car_sharing_app.model.Car;
import mate.academy.car_sharing_app.repository.CarRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
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
        Car car = carRepository.findById(id).orElseThrow(
                () -> new CarException("Can`t find car by id: " + id));
        return carMapper.toDto(car);
    }

    @Override
    public CarResponseDto updateCar(Long id, CarRequestDto carRequestDto) {
        Car car = carRepository.findById(id).orElseThrow(
                () -> new CarException("Can`t find car by id: " + id));
        car.setInventory(carRequestDto.getInventory());
        car.setModel(carRequestDto.getModel());
        car.setType(carRequestDto.getType());
        car.setDailyFee(carRequestDto.getDailyFee());
        car.setBrand(carRequestDto.getBrand());
        carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Override
    public void deleteById(Long id) {
        Car car = carRepository.findById(id).orElseThrow(
                () -> new CarException("Can`t find car by id: " + id));
        carRepository.deleteById(id);
    }
}
