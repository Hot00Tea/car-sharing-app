package mate.academy.car_sharing_app.service.rentalService;

import mate.academy.car_sharing_app.dto.rentalDto.RentalCarResponseDto;
import mate.academy.car_sharing_app.dto.rentalDto.RentalRequestDto;
import mate.academy.car_sharing_app.dto.rentalDto.RentalResponseDto;
import mate.academy.car_sharing_app.exception.EntityNotFoundException;
import mate.academy.car_sharing_app.exception.RentalException;
import mate.academy.car_sharing_app.mapper.RentalMapper;
import mate.academy.car_sharing_app.model.car.Car;
import mate.academy.car_sharing_app.model.payment.PaymentStatus;
import mate.academy.car_sharing_app.model.rental.Rental;
import mate.academy.car_sharing_app.model.user.Role;
import mate.academy.car_sharing_app.model.user.User;
import mate.academy.car_sharing_app.repository.CarRepository;
import mate.academy.car_sharing_app.repository.PaymentRepository;
import mate.academy.car_sharing_app.repository.RentalRepository;
import mate.academy.car_sharing_app.repository.UserRepository;
import mate.academy.car_sharing_app.service.notificationService.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalServiceImplTest {

    private static final String USER_EMAIL = "customer@test.com";
    private static final Long USER_ID = 1L;
    private static final Long CAR_ID = 10L;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private RentalServiceImpl rentalService;

    private User customer;
    private Car car;
    private RentalRequestDto requestDto;
    private Rental rental;
    private RentalResponseDto responseDto;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(USER_ID);
        customer.setEmail(USER_EMAIL);
        customer.setRole(Role.CUSTOMER);

        car = new Car();
        car.setId(CAR_ID);
        car.setInventory(3);
        car.setBrand("Toyota");
        car.setModel("Camry");
        car.setDailyFee(BigDecimal.valueOf(50));

        requestDto = new RentalRequestDto();
        requestDto.setCarId(CAR_ID);

        rental = new Rental();

        responseDto = new RentalResponseDto();

        RentalCarResponseDto rentalCarResponseDto =
                new RentalCarResponseDto();
        rentalCarResponseDto.setBrand("Toyota");
        rentalCarResponseDto.setModel("Camry");
        rentalCarResponseDto.setType(car.getType());
        rentalCarResponseDto.setDailyFee(car.getDailyFee());

        responseDto.setCar(rentalCarResponseDto);
    }

    @Test
    void createRental_shouldCreateRentalAndDecreaseInventory() {
        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(customer));
        when(paymentRepository.existsByRentalUserIdAndStatusIn(
                USER_ID,
                List.of(PaymentStatus.EXPIRED, PaymentStatus.PENDING)))
                .thenReturn(false);
        when(carRepository.findById(CAR_ID))
                .thenReturn(Optional.of(car));
        when(rentalMapper.toEntity(requestDto)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(responseDto);
        when(rentalRepository.save(rental)).thenReturn(rental);

        RentalResponseDto result =
                rentalService.createRental(USER_EMAIL, requestDto);

        assertNotNull(result);
        assertEquals(2, car.getInventory());
        assertEquals(customer, rental.getUser());
        assertEquals(car, rental.getCar());
        assertNotNull(rental.getRentalDate());

        verify(carRepository).save(car);
        verify(rentalRepository).save(rental);
        verify(notificationService).sendMessage(any(String.class));
    }

    @Test
    void createRental_shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.createRental(USER_EMAIL, requestDto)
        );

        verify(carRepository, never()).findById(any());
        verify(rentalRepository, never()).save(any(Rental.class));
        verify(notificationService, never()).sendMessage(any(String.class));
    }

    @Test
    void createRental_shouldThrowWhenUserHasUnpaidPayment() {
        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(customer));
        when(paymentRepository.existsByRentalUserIdAndStatusIn(
                USER_ID,
                List.of(PaymentStatus.EXPIRED, PaymentStatus.PENDING)))
                .thenReturn(true);

        assertThrows(
                RentalException.class,
                () -> rentalService.createRental(USER_EMAIL, requestDto)
        );

        verify(carRepository, never()).findById(any());
        verify(rentalRepository, never()).save(any(Rental.class));
        verify(notificationService, never()).sendMessage(any(String.class));
    }

    @Test
    void createRental_shouldThrowWhenCarDoesNotExist() {
        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(customer));
        when(paymentRepository.existsByRentalUserIdAndStatusIn(
                USER_ID,
                List.of(PaymentStatus.EXPIRED, PaymentStatus.PENDING)))
                .thenReturn(false);
        when(carRepository.findById(CAR_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.createRental(USER_EMAIL, requestDto)
        );

        verify(rentalRepository, never()).save(any(Rental.class));
        verify(notificationService, never()).sendMessage(any(String.class));
    }

    @Test
    void createRental_shouldThrowWhenCarIsOutOfStock() {
        car.setInventory(0);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(customer));
        when(paymentRepository.existsByRentalUserIdAndStatusIn(
                USER_ID,
                List.of(PaymentStatus.EXPIRED, PaymentStatus.PENDING)))
                .thenReturn(false);
        when(carRepository.findById(CAR_ID))
                .thenReturn(Optional.of(car));

        assertThrows(
                RentalException.class,
                () -> rentalService.createRental(USER_EMAIL, requestDto)
        );

        assertEquals(0, car.getInventory());

        verify(carRepository, never()).save(any(Car.class));
        verify(rentalRepository, never()).save(any(Rental.class));
        verify(notificationService, never()).sendMessage(any(String.class));
    }

    @Test
    void returnRental_shouldReturnCarAndIncreaseInventory() {
        Long rentalId = 100L;

        Rental rental = new Rental();
        rental.setId(rentalId);
        rental.setUser(customer);
        rental.setCar(car);
        rental.setActualReturnDate(null);

        car.setInventory(2);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(customer));
        when(rentalRepository.findById(rentalId))
                .thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental))
                .thenReturn(responseDto);

        RentalResponseDto result =
                rentalService.returnRental(USER_EMAIL, rentalId);

        assertNotNull(result);
        assertNotNull(rental.getActualReturnDate());
        assertEquals(3, car.getInventory());

        verify(carRepository).save(car);
        verify(rentalRepository).save(rental);
    }

    @Test
    void returnRental_shouldThrowWhenRentalDoesNotExist() {
        Long rentalId = 100L;

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(customer));
        when(rentalRepository.findById(rentalId))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.returnRental(USER_EMAIL, rentalId)
        );

        verify(carRepository, never()).save(any(Car.class));
        verify(rentalRepository, never()).save(any(Rental.class));
    }

    @Test
    void returnRental_shouldThrowWhenRentalBelongsToAnotherUser() {
        Long rentalId = 100L;

        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("another@test.com");
        anotherUser.setRole(Role.CUSTOMER);

        Rental rental = new Rental();
        rental.setId(rentalId);
        rental.setUser(anotherUser);
        rental.setCar(car);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(customer));
        when(rentalRepository.findById(rentalId))
                .thenReturn(Optional.of(rental));

        assertThrows(
                RentalException.class,
                () -> rentalService.returnRental(USER_EMAIL, rentalId)
        );

        verify(carRepository, never()).save(any(Car.class));
        verify(rentalRepository, never()).save(any(Rental.class));
    }

    @Test
    void getById_shouldReturnRentalForOwner() {
        Long rentalId = 100L;

        Rental rental = new Rental();
        rental.setId(rentalId);
        rental.setUser(customer);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(customer));
        when(rentalRepository.findById(rentalId))
                .thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental))
                .thenReturn(responseDto);

        RentalResponseDto result = rentalService.getById(USER_EMAIL, rentalId);

        assertNotNull(result);
        verify(rentalMapper).toDto(rental);
    }

    @Test
    void getById_shouldThrowWhenRentalDoesNotExist() {
        Long rentalId = 100L;

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(customer));
        when(rentalRepository.findById(rentalId))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.getById(USER_EMAIL, rentalId)
        );

        verify(rentalMapper, never()).toDto(any(Rental.class));
    }

    @Test
    void getById_shouldThrowWhenCustomerAccessesAnotherUsersRental() {
        Long rentalId = 100L;

        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setRole(Role.CUSTOMER);

        Rental rental = new Rental();
        rental.setId(rentalId);
        rental.setUser(anotherUser);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(customer));
        when(rentalRepository.findById(rentalId))
                .thenReturn(Optional.of(rental));

        assertThrows(
                RentalException.class,
                () -> rentalService.getById(USER_EMAIL, rentalId)
        );

        verify(rentalMapper, never()).toDto(any(Rental.class));
    }

    @Test
    void getById_shouldAllowManagerToAccessAnotherUsersRental() {
        Long rentalId = 100L;

        User manager = new User();
        manager.setId(3L);
        manager.setEmail("manager@test.com");
        manager.setRole(Role.MANAGER);

        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setRole(Role.CUSTOMER);

        Rental rental = new Rental();
        rental.setId(rentalId);
        rental.setUser(anotherUser);

        when(userRepository.findByEmail(manager.getEmail()))
                .thenReturn(Optional.of(manager));
        when(rentalRepository.findById(rentalId))
                .thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental))
                .thenReturn(responseDto);

        RentalResponseDto result =
                rentalService.getById(manager.getEmail(), rentalId);

        assertNotNull(result);
        verify(rentalMapper).toDto(rental);
    }

    @Test
    void returnRental_shouldThrowWhenRentalAlreadyReturned() {
        Long rentalId = 100L;

        Rental rental = new Rental();
        rental.setId(rentalId);
        rental.setUser(customer);
        rental.setCar(car);
        rental.setActualReturnDate(java.time.LocalDate.now().minusDays(1));

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(customer));
        when(rentalRepository.findById(rentalId))
                .thenReturn(Optional.of(rental));

        assertThrows(
                RentalException.class,
                () -> rentalService.returnRental(USER_EMAIL, rentalId)
        );

        verify(carRepository, never()).save(any(Car.class));
        verify(rentalRepository, never()).save(any(Rental.class));
    }

    @Test
    void getAll_shouldReturnCustomerRentalsWithoutStatusFilter() {
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        org.springframework.data.domain.Page<Rental> rentals =
                new org.springframework.data.domain.PageImpl<>(List.of(rental));

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(customer));
        when(rentalRepository.findAllByUserId(USER_ID, pageable))
                .thenReturn(rentals);
        when(rentalMapper.toDto(rental)).thenReturn(responseDto);

        var result = rentalService.getAll(USER_EMAIL, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(rentalRepository).findAllByUserId(USER_ID, pageable);
    }

    @Test
    void getAll_shouldReturnOnlyActiveCustomerRentals() {
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(customer));
        when(rentalRepository.findAllByUserIdAndActualReturnDateIsNull(
                USER_ID, pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(rental)));
        when(rentalMapper.toDto(rental)).thenReturn(responseDto);

        var result = rentalService.getAll(USER_EMAIL, null, true, pageable);

        assertEquals(1, result.getTotalElements());
        verify(rentalRepository)
                .findAllByUserIdAndActualReturnDateIsNull(USER_ID, pageable);
    }

    @Test
    void getAll_shouldReturnOnlyCompletedCustomerRentals() {
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(customer));
        when(rentalRepository.findAllByUserIdAndActualReturnDateIsNotNull(
                USER_ID, pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(rental)));
        when(rentalMapper.toDto(rental)).thenReturn(responseDto);

        var result = rentalService.getAll(USER_EMAIL, null, false, pageable);

        assertEquals(1, result.getTotalElements());
        verify(rentalRepository)
                .findAllByUserIdAndActualReturnDateIsNotNull(USER_ID, pageable);
    }

    @Test
    void getAll_shouldReturnAllRentalsForManagerWithoutFilters() {
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        User manager = new User();
        manager.setId(3L);
        manager.setEmail("manager@test.com");
        manager.setRole(Role.MANAGER);

        when(userRepository.findByEmail(manager.getEmail()))
                .thenReturn(Optional.of(manager));
        when(rentalRepository.findAll(pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(rental)));
        when(rentalMapper.toDto(rental)).thenReturn(responseDto);

        var result = rentalService.getAll(manager.getEmail(), null, null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(rentalRepository).findAll(pageable);
    }

    @Test
    void getAll_shouldFilterManagerRentalsByUserId() {
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        Long filterUserId = 2L;

        User manager = new User();
        manager.setId(3L);
        manager.setEmail("manager@test.com");
        manager.setRole(Role.MANAGER);

        when(userRepository.findByEmail(manager.getEmail()))
                .thenReturn(Optional.of(manager));
        when(rentalRepository.findAllByUserIdAndActualReturnDateIsNull(
                filterUserId, pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(rental)));
        when(rentalMapper.toDto(rental)).thenReturn(responseDto);

        var result = rentalService.getAll(
                manager.getEmail(), filterUserId, true, pageable);

        assertEquals(1, result.getTotalElements());
        verify(rentalRepository)
                .findAllByUserIdAndActualReturnDateIsNull(filterUserId, pageable);
    }
}
