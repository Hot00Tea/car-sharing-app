package mate.academy.car_sharing_app.service.scheduler;

import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import mate.academy.car_sharing_app.service.paymentService.PaymentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentExpirationScheduler {

    private final PaymentService paymentService;

    @Scheduled(cron = "0 * * * * *")
    public void checkExpiredPayments() throws StripeException {
        paymentService.checkExpiredPayments();
    }
}
