package mate.academy.car_sharing_app.service.paymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import mate.academy.car_sharing_app.exception.PaymentException;
import mate.academy.car_sharing_app.model.payment.PaymentType;
import mate.academy.car_sharing_app.model.rental.Rental;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentAmountCalculator {

    @Value("${FINE_MULTIPLIER}")
    private BigDecimal fineMultiplier;

    public BigDecimal calculate(Rental rental, PaymentType paymentType) {
        if (paymentType == PaymentType.PAYMENT) {
            return calculateRentalPayment(rental);
        }

        if (paymentType == PaymentType.FINE) {
            return calculateFine(rental);
        }

        throw new PaymentException("Unknown payment type");
    }

    private BigDecimal calculateRentalPayment(Rental rental) {
        long rentalDays = ChronoUnit.DAYS.between(
                rental.getRentalDate(),
                rental.getReturnDate()
        );

        return rental.getCar().getDailyFee()
                .multiply(BigDecimal.valueOf(rentalDays));
    }

    private BigDecimal calculateFine(Rental rental) {
        LocalDate endDate = rental.getActualReturnDate() != null
                ? rental.getActualReturnDate()
                : LocalDate.now();

        long overdueDays = ChronoUnit.DAYS.between(
                rental.getReturnDate(),
                endDate
        );

        if (overdueDays <= 0) {
            throw new PaymentException("No overdue days");
        }

        return fineMultiplier
                .multiply(rental.getCar().getDailyFee()
                        .multiply(BigDecimal.valueOf(overdueDays)));
    }
}
