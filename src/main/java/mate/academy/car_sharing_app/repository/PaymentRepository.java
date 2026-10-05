package mate.academy.car_sharing_app.repository;

import mate.academy.car_sharing_app.model.payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findAllByRentalUserEmail(String email, Pageable pageable);

    Page<Payment> findAllByRentalUserId(Long userId, Pageable pageable);

    Optional<Payment> findBySessionId(String sessionId);
}
