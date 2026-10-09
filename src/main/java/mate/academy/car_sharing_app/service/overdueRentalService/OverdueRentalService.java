package mate.academy.car_sharing_app.service.overdueRentalService;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.car_sharing_app.model.rental.Rental;
import mate.academy.car_sharing_app.repository.RentalRepository;
import mate.academy.car_sharing_app.service.notificationService.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OverdueRentalService {

    private static final String OVERDUE_RENTALS_CRON = "0 0 9 * * *";

    private final RentalRepository rentalRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = OVERDUE_RENTALS_CRON)
    public void checkOverdueRentals() {
        LocalDate today = LocalDate.now();

        List<Rental> overdueRentals =
                rentalRepository
                        .findAllByReturnDateLessThanEqualAndActualReturnDateIsNull(
                                today
                        );

        if (overdueRentals.isEmpty()) {
            notificationService.sendMessage("No rentals overdue today!");
            return;
        }

        for (Rental rental : overdueRentals) {
            notificationService.sendMessage(
                    buildOverdueRentalMessage(rental)
            );
        }
    }

    private String buildOverdueRentalMessage(Rental rental) {
        return String.format(
                """
                ⚠️ Overdue rental!

                Rental ID: %d
                User ID: %d

                Car: %s %s
                Type: %s
                Daily fee: %.2f

                Return date: %s
                """,
                rental.getId(),
                rental.getUser().getId(),
                rental.getCar().getBrand(),
                rental.getCar().getModel(),
                rental.getCar().getType(),
                rental.getCar().getDailyFee(),
                rental.getReturnDate()
        );
    }
}
