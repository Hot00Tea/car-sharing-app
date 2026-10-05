package mate.academy.car_sharing_app.service.stripeService;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {

    @Value("${app.base-url}")
    private String baseUrl;

    private final StripeClient stripeClient;

    @Override
    public Session createPaymentSession(BigDecimal amount) throws StripeException {

        String successUrl = baseUrl
                + "/payments/success?session_id={CHECKOUT_SESSION_ID}";

        String cancelUrl = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/payments/cancel")
                .toUriString();


        SessionCreateParams param = SessionCreateParams
                .builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                        .addLineItem(SessionCreateParams
                                .LineItem
                                .builder()
                                .setQuantity(1L)
                                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("usd")
                                        .setUnitAmount(amount.movePointRight(2).longValueExact())
                                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData
                                                .builder()
                                                .setName("Car rental")
                                                .build())
                                        .build())
                                .build())
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .build();
        return stripeClient.v1().checkout().sessions().create(param);
    }

    @Override
    public Session findStripeSessionBySessionId(String sessionId) throws StripeException {
        return stripeClient.v1().checkout().sessions().retrieve(sessionId);
    }
}
