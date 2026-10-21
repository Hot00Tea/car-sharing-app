package mate.academy.car_sharing_app.dto.rentalDto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RentalRequestDto {

    @NotNull
    @Future
    private LocalDate returnDate;

    @NotNull
    @Positive
    private Long carId;
}
