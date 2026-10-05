package mate.academy.car_sharing_app.mapper;

import mate.academy.car_sharing_app.dto.paymentDto.PaymentRequestDto;
import mate.academy.car_sharing_app.dto.paymentDto.PaymentResponseDto;
import mate.academy.car_sharing_app.model.payment.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rental", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "sessionUrl", ignore = true)
    @Mapping(target = "sessionId", ignore = true)
    @Mapping(target = "amountToPay", ignore = true)
    Payment toEntity(PaymentRequestDto dto);

    @Mapping(target = "rentalId", source = "rental.id")
    PaymentResponseDto toDto(Payment payment);
}
