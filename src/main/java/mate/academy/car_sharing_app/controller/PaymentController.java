package mate.academy.car_sharing_app.controller;

import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.car_sharing_app.dto.paymentDto.PaymentRequestDto;
import mate.academy.car_sharing_app.dto.paymentDto.PaymentResponseDto;
import mate.academy.car_sharing_app.service.paymentService.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment management", description = "Endpoint for managing payments")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new payment", description = "Create a new payment")
    public PaymentResponseDto createPayment(
            Authentication authentication,
            @Valid @RequestBody PaymentRequestDto requestDto)
            throws StripeException {
        return paymentService.createPayment(
                authentication.getName(),
                requestDto
        );
    }

    @GetMapping
    @Operation(summary = "Get all payments", description = "Get all payments")
    public Page<PaymentResponseDto> getAll(
            Authentication authentication,
            @RequestParam(value = "user_id", required = false) Long userId,
            Pageable pageable) {
        return paymentService.getAll(
                authentication.getName(),
                userId,
                pageable
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by id", description = "Get payment by id")
    public PaymentResponseDto getById(
            Authentication authentication,
            @PathVariable Long id) {
        return paymentService.getById(authentication.getName(), id);
    }

    @GetMapping("/success")
    public PaymentResponseDto successPay(
            @RequestParam("session_id") String sessionId)
            throws StripeException {
        return paymentService.successPay(sessionId);
    }

    @GetMapping("/cancel")
    public String cancelPay() {
        return "Payment was cancelled. You can pay later.";
    }

    @PostMapping("/{id}/renew")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Renew payment", description = "Renew payment")
    public PaymentResponseDto renewPayment(
            Authentication authentication,
            @PathVariable("id") Long paymentId) throws StripeException {
        return paymentService.renewPay(
                authentication.getName(),
                paymentId
        );
    }
}
