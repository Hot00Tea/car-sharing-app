package mate.academy.car_sharing_app.service.rentalService;

import mate.academy.car_sharing_app.dto.rentalDto.RentalRequestDto;
import mate.academy.car_sharing_app.dto.rentalDto.RentalResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {

    RentalResponseDto createRental(String email, RentalRequestDto requestDto);

    RentalResponseDto returnRental(String email, Long rentalId);

    Page<RentalResponseDto> getAll(
            String email,
            Long userId,
            Boolean isActive,
            Pageable pageable
    );

    RentalResponseDto getById(String email, Long id);
}
