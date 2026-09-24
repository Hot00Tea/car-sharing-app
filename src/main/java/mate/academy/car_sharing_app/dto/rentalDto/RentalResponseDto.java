package mate.academy.car_sharing_app.dto.rentalDto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RentalResponseDto {

    private Long id;

    private LocalDate rentalDate;

    private LocalDate returnDate;

    private LocalDate actualReturnDate;

    private RentalCarResponseDto car;

    private Long userId;
}
