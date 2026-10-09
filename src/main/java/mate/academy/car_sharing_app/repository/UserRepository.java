package mate.academy.car_sharing_app.repository;

import java.util.Optional;
import mate.academy.car_sharing_app.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
