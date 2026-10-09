package mate.academy.car_sharing_app.service.rentalService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mate.academy.car_sharing_app.dto.rentalDto.RentalRequestDto;
import mate.academy.car_sharing_app.dto.rentalDto.RentalResponseDto;
import mate.academy.car_sharing_app.exception.CarException;
import mate.academy.car_sharing_app.exception.RentalException;
import mate.academy.car_sharing_app.exception.UserException;
import mate.academy.car_sharing_app.mapper.RentalMapper;
import mate.academy.car_sharing_app.model.car.Car;
import mate.academy.car_sharing_app.model.payment.PaymentStatus;
import mate.academy.car_sharing_app.model.rental.Rental;
import mate.academy.car_sharing_app.model.user.User;
import mate.academy.car_sharing_app.repository.CarRepository;
import mate.academy.car_sharing_app.repository.PaymentRepository;
import mate.academy.car_sharing_app.repository.RentalRepository;
import mate.academy.car_sharing_app.repository.UserRepository;
import mate.academy.car_sharing_app.service.notificationService.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

import static mate.academy.car_sharing_app.model.user.Role.CUSTOMER;

@Service
@Transactional
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;

    private final CarRepository carRepository;

    private final RentalMapper rentalMapper;

    private final UserRepository userRepository;

    private final NotificationService notificationService;

    private final PaymentRepository paymentRepository;

    @Override
    public RentalResponseDto createRental(String email, RentalRequestDto requestDto) {

        User user = getUserByEmail(email);

        checkUnpaidPayments(user);

        Car car = carRepository.findById(requestDto.getCarId()).orElseThrow(
                () -> new CarException("Can't find car by id: " + requestDto.getCarId())
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
        RentalResponseDto responseDto = rentalMapper.toDto(rental);

        notificationService.sendMessage(
                buildRentalNotification(responseDto));
        return responseDto;
    }

    @Override
    public RentalResponseDto returnRental(String email, Long rentalId) {

        User user = getUserByEmail(email);

        Rental rental = rentalRepository.findById(rentalId).orElseThrow(
                () -> new RentalException("Can`t find rental by id: " + rentalId)
        );

        checkRentalOwnership(rental, user);

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

        User user = getUserByEmail(email);

        if (user.getRole() == CUSTOMER) {
            return getCustomerRentals(user.getId(), isActive, pageable);
        }

        return getManagerRentals(userId, isActive, pageable);
    }

    @Override
    public RentalResponseDto getById(String email, Long id) {

        User user = getUserByEmail(email);

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

    private Page<RentalResponseDto> getCustomerRentals(
            Long userId,
            Boolean isActive,
            Pageable pageable) {

        if (isActive == null) {
            return rentalRepository.findAllByUserId(userId, pageable)
                    .map(rentalMapper::toDto);
        }

        if (Boolean.TRUE.equals(isActive)) {
            return rentalRepository.findAllByUserIdAndActualReturnDateIsNull(
                            userId, pageable)
                    .map(rentalMapper::toDto);
        }

        return rentalRepository.findAllByUserIdAndActualReturnDateIsNotNull(
                        userId, pageable)
                .map(rentalMapper::toDto);
    }

    private Page<RentalResponseDto> getManagerRentals(
            Long userId,
            Boolean isActive,
            Pageable pageable) {

        if (userId == null) {
            if (isActive == null) {
                return rentalRepository.findAll(pageable)
                        .map(rentalMapper::toDto);
            }

            if (Boolean.TRUE.equals(isActive)) {
                return rentalRepository.findAllByActualReturnDateIsNull(pageable)
                        .map(rentalMapper::toDto);
            }

            return rentalRepository.findAllByActualReturnDateIsNotNull(pageable)
                    .map(rentalMapper::toDto);
        }

        if (isActive == null) {
            return rentalRepository.findAllByUserId(userId, pageable)
                    .map(rentalMapper::toDto);
        }

        if (Boolean.TRUE.equals(isActive)) {
            return rentalRepository.findAllByUserIdAndActualReturnDateIsNull(
                            userId, pageable)
                    .map(rentalMapper::toDto);
        }

        return rentalRepository.findAllByUserIdAndActualReturnDateIsNotNull(
                        userId, pageable)
                .map(rentalMapper::toDto);
    }

    private String buildRentalNotification(RentalResponseDto rental) {
        return String.format(
                """
                🚗 New rental created!
    
                Rental ID: %d
                User ID: %d
    
                Car: %s %s
                Type: %s
                Daily fee: $%.2f
    
                Rental date: %s
                Return date: %s
                """,
                rental.getId(),
                rental.getUserId(),
                rental.getCar().getBrand(),
                rental.getCar().getModel(),
                rental.getCar().getType(),
                rental.getCar().getDailyFee(),
                rental.getRentalDate(),
                rental.getReturnDate()
        );
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UserException("Can`t find user by email: " + email)
        );
    }

    private void checkRentalOwnership(Rental rental, User user) {
        if (!rental.getUser().getId().equals(user.getId())) {
            throw new RentalException(
                    "User is not allowed to access this rental"
            );
        }
    }

    private void checkUnpaidPayments(User user) {
        boolean hasUnpaidPayment =
                paymentRepository.existsByRentalUserIdAndStatusIn(
                        user.getId(),
                        List.of(PaymentStatus.EXPIRED, PaymentStatus.PENDING)
                );

        if (hasUnpaidPayment) {
            throw new RentalException("User has an unpaid payment");
        }
    }
}
