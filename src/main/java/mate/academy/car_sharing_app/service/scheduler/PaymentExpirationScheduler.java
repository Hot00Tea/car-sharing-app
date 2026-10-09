package mate.academy.car_sharing_app.service.scheduler;

import lombok.RequiredArgsConstructor;
import mate.academy.car_sharing_app.service.paymentService.PaymentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentExpirationScheduler {

    private static final String PAYMENT_CHECK_CRON = "0 * * * * *";

    private final PaymentService paymentService;

    @Scheduled(cron = PAYMENT_CHECK_CRON)
    public void checkExpiredPayments() {
        paymentService.checkExpiredPayments();
    }
}
