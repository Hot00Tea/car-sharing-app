package mate.academy.car_sharing_app.service.paymentService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import mate.academy.car_sharing_app.exception.PaymentException;
import mate.academy.car_sharing_app.model.car.Car;
import mate.academy.car_sharing_app.model.payment.PaymentType;
import mate.academy.car_sharing_app.model.rental.Rental;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PaymentAmountCalculatorTest {

    private PaymentAmountCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PaymentAmountCalculator();
        ReflectionTestUtils.setField(
                calculator,
                "fineMultiplier",
                new BigDecimal("2.0")
        );
    }

    @Test
    void shouldCalculateRentalPayment() {
        Rental rental = createRental(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 4),
                null,
                new BigDecimal("50.00")
        );

        BigDecimal actual = calculator.calculate(
                rental,
                PaymentType.PAYMENT
        );

        assertEquals(new BigDecimal("150.00"), actual);
    }

    @Test
    void shouldCalculateFineForOverdueRental() {
        Rental rental = createRental(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 4),
                LocalDate.of(2026, 10, 7),
                new BigDecimal("50.00")
        );

        BigDecimal actual = calculator.calculate(
                rental,
                PaymentType.FINE
        );

        assertEquals(0, new BigDecimal("300.000").compareTo(actual));
    }

    @Test
    void shouldThrowExceptionWhenRentalIsNotOverdue() {
        Rental rental = createRental(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 4),
                LocalDate.of(2026, 10, 4),
                new BigDecimal("50.00")
        );

        assertThrows(
                PaymentException.class,
                () -> calculator.calculate(rental, PaymentType.FINE)
        );
    }

    private Rental createRental(
            LocalDate rentalDate,
            LocalDate returnDate,
            LocalDate actualReturnDate,
            BigDecimal dailyFee
    ) {
        Car car = new Car();
        car.setDailyFee(dailyFee);

        Rental rental = new Rental();
        rental.setRentalDate(rentalDate);
        rental.setReturnDate(returnDate);
        rental.setActualReturnDate(actualReturnDate);
        rental.setCar(car);

        return rental;
    }
}
