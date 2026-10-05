package mate.academy.car_sharing_app.dto.paymentDto;

import lombok.Getter;
import lombok.Setter;
import mate.academy.car_sharing_app.model.payment.PaymentType;

@Getter
@Setter
public class PaymentRequestDto {

    private Long rentalId;

    private PaymentType type;
}
