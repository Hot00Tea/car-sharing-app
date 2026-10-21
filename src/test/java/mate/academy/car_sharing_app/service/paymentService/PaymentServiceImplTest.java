package mate.academy.car_sharing_app.service.paymentService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.util.Optional;
import mate.academy.car_sharing_app.dto.paymentDto.PaymentRequestDto;
import mate.academy.car_sharing_app.dto.paymentDto.PaymentResponseDto;
import mate.academy.car_sharing_app.exception.PaymentException;
import mate.academy.car_sharing_app.exception.EntityNotFoundException;
import mate.academy.car_sharing_app.exception.RentalException;
import mate.academy.car_sharing_app.mapper.PaymentMapper;
import mate.academy.car_sharing_app.model.payment.Payment;
import mate.academy.car_sharing_app.model.payment.PaymentStatus;
import mate.academy.car_sharing_app.model.payment.PaymentType;
import mate.academy.car_sharing_app.model.rental.Rental;
import mate.academy.car_sharing_app.model.user.Role;
import mate.academy.car_sharing_app.model.user.User;
import mate.academy.car_sharing_app.repository.PaymentRepository;
import mate.academy.car_sharing_app.repository.RentalRepository;
import mate.academy.car_sharing_app.repository.UserRepository;
import mate.academy.car_sharing_app.service.notificationService.NotificationService;
import mate.academy.car_sharing_app.service.stripeService.StripeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class PaymentServiceImplTest {

    private PaymentRepository paymentRepository;
    private RentalRepository rentalRepository;
    private UserRepository userRepository;
    private PaymentMapper paymentMapper;
    private StripeService stripeService;
    private NotificationService notificationService;
    private PaymentAmountCalculator paymentAmountCalculator;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        rentalRepository = mock(RentalRepository.class);
        userRepository = mock(UserRepository.class);
        paymentMapper = mock(PaymentMapper.class);
        stripeService = mock(StripeService.class);
        notificationService = mock(NotificationService.class);
        paymentAmountCalculator = mock(PaymentAmountCalculator.class);

        paymentService = new PaymentServiceImpl(
                paymentRepository,
                rentalRepository,
                userRepository,
                paymentMapper,
                stripeService,
                notificationService,
                paymentAmountCalculator
        );
    }

    @Test
    void successPay_shouldMarkPaymentAsPaidAndSendNotification() throws Exception {
        String sessionId = "cs_test_123";
        User user = new User();
        user.setId(1L);

        Rental rental = new Rental();
        rental.setId(10L);
        rental.setUser(user);

        Payment payment = new Payment();
        payment.setId(5L);
        payment.setRental(rental);
        payment.setAmountToPay(new BigDecimal("150.00"));
        payment.setType(PaymentType.PAYMENT);
        payment.setStatus(PaymentStatus.PENDING);

        Session session = mock(Session.class);
        when(session.getPaymentStatus()).thenReturn("paid");

        PaymentResponseDto responseDto = mock(PaymentResponseDto.class);
        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        when(stripeService.findStripeSessionBySessionId(sessionId)).thenReturn(session);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.successPay(sessionId);

        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertEquals(responseDto, result);
        verify(paymentRepository).save(payment);
        verify(notificationService).sendMessage(anyString());
        verify(paymentMapper).toDto(payment);
    }

    @Test
    void successPay_shouldNotMarkPaymentAsPaidWhenStripePaymentIsUnpaid() throws Exception {
        String sessionId = "cs_test_456";
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);

        Session session = mock(Session.class);
        when(session.getPaymentStatus()).thenReturn("unpaid");

        PaymentResponseDto responseDto = mock(PaymentResponseDto.class);
        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        when(stripeService.findStripeSessionBySessionId(sessionId)).thenReturn(session);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.successPay(sessionId);

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(responseDto, result);
        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(notificationService);
    }

    @Test
    void createPayment_shouldSavePendingPaymentAndReturnDto() throws Exception {
        String email = "customer@example.com";
        Long rentalId = 10L;
        User user = new User();
        user.setId(1L);

        Rental rental = new Rental();
        rental.setId(rentalId);
        rental.setUser(user);

        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setRentalId(rentalId);
        requestDto.setType(PaymentType.PAYMENT);

        BigDecimal amount = new BigDecimal("150.00");
        Payment payment = new Payment();
        payment.setType(PaymentType.PAYMENT);

        Session session = mock(Session.class);
        when(session.getId()).thenReturn("cs_test_123");
        when(session.getUrl()).thenReturn("https://stripe.test/checkout");

        PaymentResponseDto responseDto = mock(PaymentResponseDto.class);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(paymentAmountCalculator.calculate(rental, PaymentType.PAYMENT)).thenReturn(amount);
        when(paymentMapper.toEntity(requestDto)).thenReturn(payment);
        when(stripeService.createPaymentSession(amount)).thenReturn(session);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.createPayment(email, requestDto);

        assertEquals(responseDto, result);
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(rental, payment.getRental());
        assertEquals(amount, payment.getAmountToPay());
        assertEquals("cs_test_123", payment.getSessionId());
        assertEquals("https://stripe.test/checkout", payment.getSessionUrl());
        verify(paymentRepository).save(payment);
        verify(stripeService).createPaymentSession(amount);
    }

    @Test
    void createPayment_shouldThrowExceptionWhenRentalBelongsToAnotherUser() {
        String email = "customer@example.com";
        User user = new User();
        user.setId(1L);

        User anotherUser = new User();
        anotherUser.setId(2L);

        Rental rental = new Rental();
        rental.setId(10L);
        rental.setUser(anotherUser);

        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setRentalId(10L);
        requestDto.setType(PaymentType.PAYMENT);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(rentalRepository.findById(10L)).thenReturn(Optional.of(rental));

        assertThrows(
                RentalException.class,
                () -> paymentService.createPayment(email, requestDto)
        );
        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(stripeService);
    }

    @Test
    void createPayment_shouldThrowExceptionWhenRentalDoesNotExist() {
        String email = "customer@example.com";
        Long rentalId = 10L;
        User user = new User();
        user.setId(1L);

        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setRentalId(rentalId);
        requestDto.setType(PaymentType.PAYMENT);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> paymentService.createPayment(email, requestDto)
        );
        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(stripeService);
    }

    @Test
    void renewPay_shouldCreateNewSessionForExpiredPayment() throws Exception {
        String email = "customer@example.com";
        Long paymentId = 5L;
        User user = new User();
        user.setId(1L);

        Rental rental = new Rental();
        rental.setUser(user);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setRental(rental);
        payment.setStatus(PaymentStatus.EXPIRED);
        payment.setAmountToPay(new BigDecimal("150.00"));
        payment.setSessionId("cs_old");

        Session session = mock(Session.class);
        when(session.getId()).thenReturn("cs_new");
        when(session.getUrl()).thenReturn("https://stripe.test/new-session");

        PaymentResponseDto responseDto = mock(PaymentResponseDto.class);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(stripeService.createPaymentSession(payment.getAmountToPay())).thenReturn(session);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.renewPay(email, paymentId);

        assertEquals(responseDto, result);
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals("cs_new", payment.getSessionId());
        assertEquals("https://stripe.test/new-session", payment.getSessionUrl());
        verify(paymentRepository).save(payment);
        verify(stripeService).createPaymentSession(payment.getAmountToPay());
    }

    @Test
    void renewPay_shouldThrowExceptionWhenPaymentIsNotExpired() {
        String email = "customer@example.com";
        Long paymentId = 5L;
        User user = new User();
        user.setId(1L);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setStatus(PaymentStatus.PENDING);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThrows(PaymentException.class, () -> paymentService.renewPay(email, paymentId));
        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(stripeService);
    }

    @Test
    void renewPay_shouldThrowExceptionWhenPaymentBelongsToAnotherUser() {
        String email = "customer@example.com";
        Long paymentId = 5L;
        User user = new User();
        user.setId(1L);

        User anotherUser = new User();
        anotherUser.setId(2L);

        Rental rental = new Rental();
        rental.setUser(anotherUser);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setRental(rental);
        payment.setStatus(PaymentStatus.EXPIRED);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThrows(PaymentException.class, () -> paymentService.renewPay(email, paymentId));
        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(stripeService);
    }

    @Test
    void renewPay_shouldThrowExceptionWhenPaymentDoesNotExist() {
        String email = "customer@example.com";
        Long paymentId = 5L;
        User user = new User();
        user.setId(1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> paymentService.renewPay(email, paymentId));
        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(stripeService);
    }

    @Test
    void getById_customerCanAccessOwnPayment() {
        String email = "customer@example.com";
        Long paymentId = 5L;

        User user = new User();
        user.setId(1L);
        user.setRole(Role.CUSTOMER);

        Rental rental = new Rental();
        rental.setUser(user);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setRental(rental);

        PaymentResponseDto responseDto = mock(PaymentResponseDto.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        assertEquals(responseDto, paymentService.getById(email, paymentId));
    }

    @Test
    void getById_customerCannotAccessAnotherUsersPayment() {
        String email = "customer@example.com";
        User user = new User();
        user.setId(1L);
        user.setRole(Role.CUSTOMER);

        User anotherUser = new User();
        anotherUser.setId(2L);

        Rental rental = new Rental();
        rental.setUser(anotherUser);

        Payment payment = new Payment();
        payment.setId(5L);
        payment.setRental(rental);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment));

        assertThrows(PaymentException.class, () -> paymentService.getById(email, 5L));
    }

    @Test
    void getById_managerCanAccessAnotherUsersPayment() {
        String email = "manager@example.com";
        User manager = new User();
        manager.setId(3L);
        manager.setRole(Role.MANAGER);

        User customer = new User();
        customer.setId(1L);

        Rental rental = new Rental();
        rental.setUser(customer);

        Payment payment = new Payment();
        payment.setId(5L);
        payment.setRental(rental);

        PaymentResponseDto responseDto = mock(PaymentResponseDto.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(manager));
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        assertEquals(responseDto, paymentService.getById(email, 5L));
    }

    @Test
    void getAll_customerGetsOnlyOwnPayments() {
        String email = "customer@example.com";
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(1L);
        user.setRole(Role.CUSTOMER);

        Payment payment = new Payment();
        Page<Payment> payments = new PageImpl<>(java.util.List.of(payment));
        PaymentResponseDto responseDto = mock(PaymentResponseDto.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(paymentRepository.findAllByRentalUserEmail(email, pageable)).thenReturn(payments);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        Page<PaymentResponseDto> result = paymentService.getAll(email, null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(paymentRepository).findAllByRentalUserEmail(email, pageable);
        verify(paymentRepository, never()).findAll(pageable);
    }

    @Test
    void getAll_managerGetsAllPaymentsWhenUserIdIsNull() {
        String email = "manager@example.com";
        Pageable pageable = PageRequest.of(0, 10);

        User manager = new User();
        manager.setId(3L);
        manager.setRole(Role.MANAGER);

        Payment payment = new Payment();
        Page<Payment> payments = new PageImpl<>(java.util.List.of(payment));
        PaymentResponseDto responseDto = mock(PaymentResponseDto.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(manager));
        when(paymentRepository.findAll(pageable)).thenReturn(payments);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        Page<PaymentResponseDto> result = paymentService.getAll(email, null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(paymentRepository).findAll(pageable);
    }

    @Test
    void getAll_managerFiltersPaymentsByUserId() {
        String email = "manager@example.com";
        Long customerId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        User manager = new User();
        manager.setId(3L);
        manager.setRole(Role.MANAGER);

        Payment payment = new Payment();
        Page<Payment> payments = new PageImpl<>(java.util.List.of(payment));
        PaymentResponseDto responseDto = mock(PaymentResponseDto.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(manager));
        when(paymentRepository.findAllByRentalUserId(customerId, pageable)).thenReturn(payments);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        Page<PaymentResponseDto> result = paymentService.getAll(email, customerId, pageable);

        assertEquals(1, result.getTotalElements());
        verify(paymentRepository).findAllByRentalUserId(customerId, pageable);
    }

    @Test
    void checkExpiredPayments_shouldMarkExpiredPaymentAndSaveIt() throws Exception {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setSessionId("cs_expired");
        payment.setStatus(PaymentStatus.PENDING);

        Session session = mock(Session.class);
        when(session.getExpiresAt()).thenReturn(1L);
        when(stripeService.findStripeSessionBySessionId("cs_expired"))
                .thenReturn(session);
        when(paymentRepository.findAllByStatus(PaymentStatus.PENDING))
                .thenReturn(java.util.List.of(payment));

        paymentService.checkExpiredPayments();

        assertEquals(PaymentStatus.EXPIRED, payment.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void checkExpiredPayments_shouldKeepActivePaymentPending() throws Exception {
        Payment payment = new Payment();
        payment.setId(2L);
        payment.setSessionId("cs_active");
        payment.setStatus(PaymentStatus.PENDING);

        Session session = mock(Session.class);
        when(session.getExpiresAt())
                .thenReturn(java.time.Instant.now().getEpochSecond() + 3600);
        when(stripeService.findStripeSessionBySessionId("cs_active"))
                .thenReturn(session);
        when(paymentRepository.findAllByStatus(PaymentStatus.PENDING))
                .thenReturn(java.util.List.of(payment));

        paymentService.checkExpiredPayments();

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void checkExpiredPayments_shouldContinueWhenStripeThrowsException()
            throws Exception {
        Payment failedPayment = new Payment();
        failedPayment.setId(3L);
        failedPayment.setSessionId("cs_error");
        failedPayment.setStatus(PaymentStatus.PENDING);

        Payment expiredPayment = new Payment();
        expiredPayment.setId(4L);
        expiredPayment.setSessionId("cs_expired");
        expiredPayment.setStatus(PaymentStatus.PENDING);

        Session expiredSession = mock(Session.class);
        when(expiredSession.getExpiresAt()).thenReturn(1L);

        when(paymentRepository.findAllByStatus(PaymentStatus.PENDING))
                .thenReturn(java.util.List.of(failedPayment, expiredPayment));

        when(stripeService.findStripeSessionBySessionId("cs_error"))
                .thenThrow(mock(com.stripe.exception.StripeException.class));
        when(stripeService.findStripeSessionBySessionId("cs_expired"))
                .thenReturn(expiredSession);

        paymentService.checkExpiredPayments();

        assertEquals(PaymentStatus.PENDING, failedPayment.getStatus());
        assertEquals(PaymentStatus.EXPIRED, expiredPayment.getStatus());

        verify(paymentRepository).save(expiredPayment);
        verify(paymentRepository, never()).save(failedPayment);
    }
}
