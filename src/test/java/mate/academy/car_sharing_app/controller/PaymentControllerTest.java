package mate.academy.car_sharing_app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stripe.exception.StripeException;
import java.util.List;
import mate.academy.car_sharing_app.dto.paymentDto.PaymentRequestDto;
import mate.academy.car_sharing_app.dto.paymentDto.PaymentResponseDto;
import mate.academy.car_sharing_app.service.paymentService.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void createPayment_shouldReturnCreatedPayment() throws StripeException {
        String email = "test@example.com";
        PaymentRequestDto requestDto = new PaymentRequestDto();
        PaymentResponseDto expectedDto = new PaymentResponseDto();

        when(authentication.getName()).thenReturn(email);
        when(paymentService.createPayment(email, requestDto)).thenReturn(expectedDto);

        PaymentResponseDto actualDto =
                paymentController.createPayment(authentication, requestDto);

        assertEquals(expectedDto, actualDto);
        verify(paymentService).createPayment(email, requestDto);
    }

    @Test
    void getAll_shouldReturnPageOfPayments() {
        String email = "test@example.com";
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentResponseDto> expectedPage = new PageImpl<>(List.of());

        when(authentication.getName()).thenReturn(email);
        when(paymentService.getAll(email, userId, pageable)).thenReturn(expectedPage);

        Page<PaymentResponseDto> actualPage =
                paymentController.getAll(authentication, userId, pageable);

        assertEquals(expectedPage, actualPage);
        verify(paymentService).getAll(email, userId, pageable);
    }

    @Test
    void getById_shouldReturnPayment() {
        String email = "test@example.com";
        Long paymentId = 1L;
        PaymentResponseDto expectedDto = new PaymentResponseDto();

        when(authentication.getName()).thenReturn(email);
        when(paymentService.getById(email, paymentId)).thenReturn(expectedDto);

        PaymentResponseDto actualDto =
                paymentController.getById(authentication, paymentId);

        assertEquals(expectedDto, actualDto);
        verify(paymentService).getById(email, paymentId);
    }

    @Test
    void successPay_shouldReturnPayment() throws StripeException {
        String sessionId = "cs_test_123";
        PaymentResponseDto expectedDto = new PaymentResponseDto();

        when(paymentService.successPay(sessionId)).thenReturn(expectedDto);

        PaymentResponseDto actualDto = paymentController.successPay(sessionId);

        assertEquals(expectedDto, actualDto);
        verify(paymentService).successPay(sessionId);
    }

    @Test
    void cancelPay_shouldReturnCancellationMessage() {
        String expectedMessage = "Payment was cancelled. You can pay later.";

        String actualMessage = paymentController.cancelPay();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void renewPayment_shouldReturnRenewedPayment() throws StripeException {
        String email = "test@example.com";
        Long paymentId = 1L;
        PaymentResponseDto expectedDto = new PaymentResponseDto();

        when(authentication.getName()).thenReturn(email);
        when(paymentService.renewPay(email, paymentId)).thenReturn(expectedDto);

        PaymentResponseDto actualDto =
                paymentController.renewPayment(authentication, paymentId);

        assertEquals(expectedDto, actualDto);
        verify(paymentService).renewPay(email, paymentId);
    }
}
