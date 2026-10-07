package mate.academy.car_sharing_app.service.paymentService;

import com.stripe.exception.StripeException;
import mate.academy.car_sharing_app.dto.paymentDto.PaymentRequestDto;
import mate.academy.car_sharing_app.dto.paymentDto.PaymentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    PaymentResponseDto createPayment(String email, PaymentRequestDto requestDto) throws StripeException;

    PaymentResponseDto getById(String email, Long id);

    Page<PaymentResponseDto> getAll(String email, Long userId, Pageable pageable);

    PaymentResponseDto successPay(String sessionId) throws StripeException;

    void checkExpiredPayments() throws StripeException;

    PaymentResponseDto renewPay(String email, Long paymentId) throws StripeException;
}
