package mate.academy.car_sharing_app.service.stripeService;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;

public interface StripeService {

    Session createPaymentSession(BigDecimal amount) throws StripeException;

    Session findStripeSessionBySessionId(String sessionId) throws StripeException;
}
