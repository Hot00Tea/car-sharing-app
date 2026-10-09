package mate.academy.car_sharing_app.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import mate.academy.car_sharing_app.model.payment.Payment;
import mate.academy.car_sharing_app.model.payment.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findAllByRentalUserEmail(
            String email,
            Pageable pageable
    );

    Page<Payment> findAllByRentalUserId(
            Long userId,
            Pageable pageable
    );

    Optional<Payment> findBySessionId(String sessionId);

    List<Payment> findAllByStatus(PaymentStatus status);

    boolean existsByRentalUserIdAndStatusIn(
            Long userId,
            Collection<PaymentStatus> statuses
    );
}
