package mate.academy.car_sharing_app.mapper;

import java.math.BigDecimal;
import mate.academy.car_sharing_app.dto.paymentDto.PaymentRequestDto;
import mate.academy.car_sharing_app.dto.paymentDto.PaymentResponseDto;
import mate.academy.car_sharing_app.model.payment.Payment;
import mate.academy.car_sharing_app.model.payment.PaymentStatus;
import mate.academy.car_sharing_app.model.payment.PaymentType;
import mate.academy.car_sharing_app.model.rental.Rental;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaymentMapperTest {

    private PaymentMapper paymentMapper;

    @BeforeEach
    void setUp() {
        paymentMapper = Mappers.getMapper(PaymentMapper.class);
    }

    @Test
    void toEntity_shouldMapTypeAndIgnoreConfiguredFields() {
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setRentalId(10L);
        requestDto.setType(PaymentType.PAYMENT);

        Payment payment = paymentMapper.toEntity(requestDto);

        assertEquals(PaymentType.PAYMENT, payment.getType());
        assertNull(payment.getId());
        assertNull(payment.getRental());
        assertNull(payment.getStatus());
        assertNull(payment.getSessionUrl());
        assertNull(payment.getSessionId());
        assertNull(payment.getAmountToPay());
    }

    @Test
    void toDto_shouldMapPaymentFieldsAndRentalId() {
        Rental rental = new Rental();
        rental.setId(10L);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setRental(rental);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setType(PaymentType.PAYMENT);
        payment.setSessionUrl("https://example.com/checkout");
        payment.setSessionId("cs_test_123");
        payment.setAmountToPay(BigDecimal.valueOf(150));

        PaymentResponseDto responseDto = paymentMapper.toDto(payment);

        assertEquals(1L, responseDto.getId());
        assertEquals(10L, responseDto.getRentalId());
        assertEquals(PaymentStatus.PENDING, responseDto.getStatus());
        assertEquals(PaymentType.PAYMENT, responseDto.getType());
        assertEquals("https://example.com/checkout", responseDto.getSessionUrl());
        assertEquals("cs_test_123", responseDto.getSessionId());
        assertEquals(BigDecimal.valueOf(150), responseDto.getAmountToPay());
    }
}
