package mate.academy.car_sharing_app.service.paymentService;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import mate.academy.car_sharing_app.dto.paymentDto.PaymentRequestDto;
import mate.academy.car_sharing_app.dto.paymentDto.PaymentResponseDto;
import mate.academy.car_sharing_app.exception.PaymentException;
import mate.academy.car_sharing_app.exception.RentalException;
import mate.academy.car_sharing_app.exception.UserException;
import mate.academy.car_sharing_app.mapper.PaymentMapper;
import mate.academy.car_sharing_app.model.payment.Payment;
import mate.academy.car_sharing_app.model.payment.PaymentStatus;
import mate.academy.car_sharing_app.model.payment.PaymentType;
import mate.academy.car_sharing_app.model.rental.Rental;
import mate.academy.car_sharing_app.model.user.User;
import mate.academy.car_sharing_app.repository.PaymentRepository;
import mate.academy.car_sharing_app.repository.RentalRepository;
import mate.academy.car_sharing_app.repository.UserRepository;
import mate.academy.car_sharing_app.service.stripeService.StripeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static mate.academy.car_sharing_app.model.user.Role.CUSTOMER;
import static mate.academy.car_sharing_app.model.user.Role.MANAGER;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final RentalRepository rentalRepository;

    private final UserRepository userRepository;

    private final PaymentMapper paymentMapper;

    private final StripeService stripeService;

    @Value("${FINE_MULTIPLIER}")
    private BigDecimal fineMultiplier;

    @Override
    public PaymentResponseDto createPayment(String email, PaymentRequestDto requestDto) throws StripeException {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserException("Can`t find user by email: " + email)
        );
        Rental rental = rentalRepository.findById(requestDto.getRentalId()).orElseThrow(
                () -> new RentalException("Can`t find rental by id: " + requestDto.getRentalId())
        );

        if (!rental.getUser().getId().equals(user.getId())) {
            throw new RentalException("User is not allowed to access this rental");
        }

        BigDecimal amountToPay;

        if (requestDto.getType() == PaymentType.PAYMENT) {
            long rentalDays = ChronoUnit.DAYS.between(rental.getRentalDate(), rental.getReturnDate());
            amountToPay = (rental.getCar().getDailyFee().multiply(BigDecimal.valueOf(rentalDays)));
        } else if (requestDto.getType() == PaymentType.FINE){
            LocalDate endDate;

            if (rental.getActualReturnDate() != null) {
                endDate = rental.getActualReturnDate();
            } else {
                endDate = LocalDate.now();
            }
            long overdueDays = ChronoUnit.DAYS.between(rental.getReturnDate(), endDate);

            if (overdueDays <= 0) {
                throw new PaymentException("No overdue days");
            }

            amountToPay = fineMultiplier.multiply(rental.getCar().getDailyFee().multiply(BigDecimal.valueOf(overdueDays)));

        } else {
            throw new PaymentException("Unknown payment type");
        }

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

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserException("Can`t find user by email: " + email)
        );

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

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserException("Can`t find user by email: " + email)
        );

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

        if (session.getPaymentStatus().equals("paid")) {
            payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);
        }
        return paymentMapper.toDto(payment);
    }
}
