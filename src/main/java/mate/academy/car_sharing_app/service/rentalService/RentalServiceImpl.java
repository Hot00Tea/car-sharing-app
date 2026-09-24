package mate.academy.car_sharing_app.service.rentalService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mate.academy.car_sharing_app.dto.rentalDto.RentalRequestDto;
import mate.academy.car_sharing_app.dto.rentalDto.RentalResponseDto;
import mate.academy.car_sharing_app.exception.CarException;
import mate.academy.car_sharing_app.exception.RentalException;
import mate.academy.car_sharing_app.exception.UserException;
import mate.academy.car_sharing_app.mapper.RentalMapper;
import mate.academy.car_sharing_app.model.Car;
import mate.academy.car_sharing_app.model.Rental;
import mate.academy.car_sharing_app.model.User;
import mate.academy.car_sharing_app.repository.CarRepository;
import mate.academy.car_sharing_app.repository.RentalRepository;
import mate.academy.car_sharing_app.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import static mate.academy.car_sharing_app.model.Role.CUSTOMER;

@Service
@Transactional
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;

    private final CarRepository carRepository;

    private final RentalMapper rentalMapper;

    private final UserRepository userRepository;

    @Override
    public RentalResponseDto createRental(String email, RentalRequestDto requestDto) {
        Car car = carRepository.findById(requestDto.getCarId()).orElseThrow(
                () -> new CarException("Can't find car by id: " + requestDto.getCarId())
        );

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserException("Can`t find user by email: " + email)
        );

        if (car.getInventory() <= 0) {
            throw new RentalException("No cars in stock");
        }
        Rental rental = rentalMapper.toEntity(requestDto);
        rental.setRentalDate(LocalDate.now());
        rental.setUser(user);
        rental.setCar(car);
        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);
        rentalRepository.save(rental);
        return rentalMapper.toDto(rental);
    }

    @Override
    public RentalResponseDto returnRental(String email, Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(
                () -> new RentalException("Can`t find rental by id: " + rentalId)
        );

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserException("Can`t find user by email: " + email)
        );

        if (!rental.getUser().getId().equals(user.getId())) {
            throw new RentalException("User is not allowed to access this rental");
        }

        if (rental.getActualReturnDate() != null) {
            throw new RentalException("Rental has already been returned");
        }
        rental.setActualReturnDate(LocalDate.now());
        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);
        rentalRepository.save(rental);
        return rentalMapper.toDto(rental);
    }

    @Override
    public Page<RentalResponseDto> getAll(
            String email,
            Long userId,
            Boolean isActive,
            Pageable pageable) {

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserException("Can`t find user by email: " + email)
        );

        if (user.getRole() == CUSTOMER) {
            if (isActive == null) {
                return rentalRepository.findAllByUserId(user.getId(), pageable)
                        .map(rentalMapper::toDto);
            }
            if (Boolean.TRUE.equals(isActive)) {
                return rentalRepository.findAllByUserIdAndActualReturnDateIsNull(
                        user.getId(), pageable).map(rentalMapper::toDto);
            }
            if (Boolean.FALSE.equals(isActive)) {
                return rentalRepository.findAllByUserIdAndActualReturnDateIsNotNull(
                        user.getId(), pageable).map(rentalMapper::toDto);
            }
        } else {
            if (userId == null && isActive == null) {
                return rentalRepository.findAll(pageable).map(rentalMapper::toDto);
            }
            if (userId == null && Boolean.TRUE.equals(isActive)) {
                return rentalRepository.findAllByActualReturnDateIsNull(pageable)
                        .map(rentalMapper::toDto);
            }
            if (userId == null && Boolean.FALSE.equals(isActive)) {
                return rentalRepository.findAllByActualReturnDateIsNotNull(pageable)
                        .map(rentalMapper::toDto);
            }
            if (userId != null && isActive == null) {
                return rentalRepository.findAllByUserId(userId, pageable)
                        .map(rentalMapper::toDto);
            }
            if (userId != null && Boolean.TRUE.equals(isActive)) {
                return rentalRepository.findAllByUserIdAndActualReturnDateIsNull(
                        userId, pageable).map(rentalMapper::toDto);
            }
            if (userId != null && Boolean.FALSE.equals(isActive)) {
                return rentalRepository.findAllByUserIdAndActualReturnDateIsNotNull(
                        userId, pageable).map(rentalMapper::toDto);
            }

        }
        throw new IllegalStateException("Invalid rental filter");
    }

    @Override
    public RentalResponseDto getById(String email, Long id) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserException("Can`t find user by email: " + email)
        );

        Rental rental = rentalRepository.findById(id).orElseThrow(
                () -> new RentalException("Can`t find rental by id: " + id)
        );

        if (user.getRole() == CUSTOMER
                && !rental.getUser().getId().equals(user.getId())) {
            throw new UserException(
                    "User is not allowed to access this rental"
            );
        }

        return rentalMapper.toDto(rental);
    }
}
