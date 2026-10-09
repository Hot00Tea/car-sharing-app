package mate.academy.car_sharing_app.service.paymentService;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mate.academy.car_sharing_app.dto.paymentDto.PaymentRequestDto;
import mate.academy.car_sharing_app.dto.paymentDto.PaymentResponseDto;
import mate.academy.car_sharing_app.exception.PaymentException;
import mate.academy.car_sharing_app.exception.RentalException;
import mate.academy.car_sharing_app.exception.UserException;
import mate.academy.car_sharing_app.mapper.PaymentMapper;
import mate.academy.car_sharing_app.model.payment.Payment;
import mate.academy.car_sharing_app.model.payment.PaymentStatus;
import mate.academy.car_sharing_app.model.rental.Rental;
import mate.academy.car_sharing_app.model.user.User;
import mate.academy.car_sharing_app.repository.PaymentRepository;
import mate.academy.car_sharing_app.repository.RentalRepository;
import mate.academy.car_sharing_app.repository.UserRepository;
import mate.academy.car_sharing_app.service.notificationService.NotificationService;
import mate.academy.car_sharing_app.service.stripeService.StripeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static mate.academy.car_sharing_app.model.user.Role.CUSTOMER;
import static mate.academy.car_sharing_app.model.user.Role.MANAGER;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final RentalRepository rentalRepository;

    private final UserRepository userRepository;

    private final PaymentMapper paymentMapper;

    private final StripeService stripeService;

    private final NotificationService notificationService;

    private final PaymentAmountCalculator paymentAmountCalculator;

    @Override
    public PaymentResponseDto createPayment(String email, PaymentRequestDto requestDto) throws StripeException {

        User user = getUserByEmail(email);

        Rental rental = rentalRepository.findById(requestDto.getRentalId()).orElseThrow(
                () -> new RentalException("Can`t find rental by id: " + requestDto.getRentalId())
        );

        if (!rental.getUser().getId().equals(user.getId())) {
            throw new RentalException("User is not allowed to access this rental");
        }

        BigDecimal amountToPay = paymentAmountCalculator.calculate(
                rental,
                requestDto.getType()
        );

        Payment payment = paymentMapper.toEntity(requestDto);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setRental(rental);
        payment.setAmountToPay(amountToPay);
        Session session = stripeService.createPaymentSession(amountToPay);
        payment.setSessionId(session.getId());
        payment.setSessionUrl(session.getUrl());
        paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    @Override
    public PaymentResponseDto getById(String email, Long id) {

        User user = getUserByEmail(email);

        Payment payment = paymentRepository.findById(id).orElseThrow(
                () -> new PaymentException("Can`t find payment by id: " + id)
        );

        if (user.getRole() == CUSTOMER
                && !payment.getRental().getUser().getId().equals(user.getId())) {
            throw new PaymentException(
                    "User is not allowed to access this payment"
            );
        }

        return paymentMapper.toDto(payment);
    }

    @Override
    public Page<PaymentResponseDto> getAll(String email, Long userId, Pageable pageable) {

        User user = getUserByEmail(email);

        if (user.getRole() == CUSTOMER) {
             return paymentRepository.findAllByRentalUserEmail(email, pageable)
                     .map(paymentMapper::toDto);
        } else if (user.getRole() == MANAGER) {

            if (userId == null) {
                return paymentRepository.findAll(pageable)
                        .map(paymentMapper::toDto);
            }
            return paymentRepository.findAllByRentalUserId(userId, pageable)
                    .map(paymentMapper::toDto);
        }
        throw new PaymentException("Invalid payment filter");
    }

    @Override
    public PaymentResponseDto successPay(String sessionId) throws StripeException {
        
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new PaymentException("Can't find payment by session id: " + sessionId)
        );

        Session session = stripeService.findStripeSessionBySessionId(sessionId);

        if ("paid".equals(session.getPaymentStatus())) {
            payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);
            String message = String.format(
                    """
                    💳 Payment successful!
            
                    Payment ID: %d
                    Rental ID: %d
                    User ID: %d
            
                    Amount: %.2f USD
                    Type: %s
                    Status: %s
                    """,
                    payment.getId(),
                    payment.getRental().getId(),
                    payment.getRental().getUser().getId(),
                    payment.getAmountToPay(),
                    payment.getType(),
                    payment.getStatus()
            );
            notificationService.sendMessage(message);
        }
        return paymentMapper.toDto(payment);
    }

    @Override
    public void checkExpiredPayments() {
        List<Payment> paymentList =
                paymentRepository.findAllByStatus(PaymentStatus.PENDING);

        for (Payment payment : paymentList) {
            checkPaymentExpiration(payment);
        }
    }

    @Override
    public PaymentResponseDto renewPay(String email, Long paymentId)
            throws StripeException {

        User user = getUserByEmail(email);

        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
                () -> new PaymentException("Can`t find payment by id: " + paymentId)
        );

        if (payment.getStatus() != PaymentStatus.EXPIRED) {
            throw new PaymentException(
                    "Payment can be renewed only if its status is EXPIRED"
            );
        }

        if (!payment.getRental().getUser().getId().equals(user.getId())) {
            throw new PaymentException(
                    "User is not allowed to renew this payment"
            );
        }

        Session session = stripeService.createPaymentSession(
                payment.getAmountToPay()
        );

        payment.setStatus(PaymentStatus.PENDING);
        payment.setSessionId(session.getId());
        payment.setSessionUrl(session.getUrl());

        paymentRepository.save(payment);

        return paymentMapper.toDto(payment);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UserException("Can`t find user by email: " + email)
        );
    }

    private void checkPaymentExpiration(Payment payment) {
        try {
            Session session = stripeService.findStripeSessionBySessionId(
                    payment.getSessionId()
            );

            if (session.getExpiresAt() < Instant.now().getEpochSecond()) {
                payment.setStatus(PaymentStatus.EXPIRED);
                paymentRepository.save(payment);
            }
        } catch (StripeException e) {
            log.error("Failed to check payment expiration: {}", payment.getId(), e);
        }
    }
}
