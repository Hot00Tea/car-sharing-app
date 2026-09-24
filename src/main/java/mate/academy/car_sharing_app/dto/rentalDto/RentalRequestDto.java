package mate.academy.car_sharing_app.dto.rentalDto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RentalRequestDto {

    private LocalDate returnDate;

    private Long carId;
}
