package mate.academy.car_sharing_app.repository;

import mate.academy.car_sharing_app.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {
}
