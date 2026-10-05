package mate.academy.car_sharing_app.dto.paymentDto;

import lombok.Data;
import mate.academy.car_sharing_app.model.payment.PaymentStatus;
import mate.academy.car_sharing_app.model.payment.PaymentType;
import java.math.BigDecimal;

@Data
public class PaymentResponseDto {

    private Long id;

    private Long rentalId;

    private PaymentStatus status;

    private PaymentType type;

    private String sessionUrl;

    private String sessionId;

    private BigDecimal amountToPay;
}
