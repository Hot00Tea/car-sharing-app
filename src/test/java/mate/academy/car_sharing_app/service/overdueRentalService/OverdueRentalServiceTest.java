package mate.academy.car_sharing_app.service.overdueRentalService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import mate.academy.car_sharing_app.model.car.Car;
import mate.academy.car_sharing_app.model.rental.Rental;
import mate.academy.car_sharing_app.model.user.User;
import mate.academy.car_sharing_app.repository.RentalRepository;
import mate.academy.car_sharing_app.service.notificationService.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OverdueRentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OverdueRentalService overdueRentalService;

    @Test
    void checkOverdueRentals_shouldSendMessageWhenNoOverdueRentals() {
        when(rentalRepository
                .findAllByReturnDateLessThanEqualAndActualReturnDateIsNull(
                        LocalDate.now()
                ))
                .thenReturn(List.of());

        overdueRentalService.checkOverdueRentals();

        verify(notificationService).sendMessage("No rentals overdue today!");
        verify(notificationService, times(1)).sendMessage(anyString());
    }

    @Test
    void checkOverdueRentals_shouldSendMessageForOverdueRental() {
        Rental rental = createRental(1L, 10L, "Toyota", "Camry");

        when(rentalRepository
                .findAllByReturnDateLessThanEqualAndActualReturnDateIsNull(
                        LocalDate.now()
                ))
                .thenReturn(List.of(rental));

        overdueRentalService.checkOverdueRentals();

        verify(notificationService).sendMessage(
                org.mockito.ArgumentMatchers.argThat(message ->
                        message.contains("Rental ID: 1")
                                && message.contains("User ID: 10")
                                && message.contains("Car: Toyota Camry")
                                && message.contains("Daily fee: 50,00")
                )
        );
    }

    @Test
    void checkOverdueRentals_shouldSendMessageForEachOverdueRental() {
        Rental firstRental = createRental(1L, 10L, "Toyota", "Camry");
        Rental secondRental = createRental(2L, 20L, "BMW", "X5");

        when(rentalRepository
                .findAllByReturnDateLessThanEqualAndActualReturnDateIsNull(
                        LocalDate.now()
                ))
                .thenReturn(List.of(firstRental, secondRental));

        overdueRentalService.checkOverdueRentals();

        verify(notificationService, times(2)).sendMessage(anyString());
        verify(notificationService, never())
                .sendMessage("No rentals overdue today!");
    }

    private Rental createRental(
            Long rentalId,
            Long userId,
            String brand,
            String model
    ) {
        User user = new User();
        user.setId(userId);

        Car car = new Car();
        car.setBrand(brand);
        car.setModel(model);
        car.setType(mate.academy.car_sharing_app.model.car.CarType.SEDAN);
        car.setDailyFee(BigDecimal.valueOf(50));

        Rental rental = new Rental();
        rental.setId(rentalId);
        rental.setUser(user);
        rental.setCar(car);
        rental.setReturnDate(LocalDate.now().minusDays(1));

        return rental;
    }
}
