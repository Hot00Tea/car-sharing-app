package mate.academy.car_sharing_app.repository;

import mate.academy.car_sharing_app.model.Rental;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    Page<Rental> findAllByUserId(Long id, Pageable pageable);

    Page<Rental> findAllByUserIdAndActualReturnDateIsNull(
            Long id,
            Pageable pageable
    );

    Page<Rental> findAllByUserIdAndActualReturnDateIsNotNull(Long id, Pageable pageable);

    Page<Rental> findAllByActualReturnDateIsNull(Pageable pageable);

    Page<Rental> findAllByActualReturnDateIsNotNull(Pageable pageable);

    List<Rental> findAllByReturnDateLessThanEqualAndActualReturnDateIsNull(LocalDate date);
}