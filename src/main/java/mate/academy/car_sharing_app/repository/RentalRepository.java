package mate.academy.car_sharing_app.repository;

import java.time.LocalDate;
import java.util.List;
import mate.academy.car_sharing_app.model.rental.Rental;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    Page<Rental> findAllByUserId(Long userId, Pageable pageable);

    Page<Rental> findAllByUserIdAndActualReturnDateIsNull(
            Long userId,
            Pageable pageable
    );

    Page<Rental> findAllByUserIdAndActualReturnDateIsNotNull(
            Long userId,
            Pageable pageable
    );

    Page<Rental> findAllByActualReturnDateIsNull(Pageable pageable);

    Page<Rental> findAllByActualReturnDateIsNotNull(Pageable pageable);

    List<Rental> findAllByReturnDateLessThanEqualAndActualReturnDateIsNull(
            LocalDate date
    );
}
