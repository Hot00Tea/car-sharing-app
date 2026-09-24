package mate.academy.car_sharing_app.mapper;

import mate.academy.car_sharing_app.dto.rentalDto.RentalRequestDto;
import mate.academy.car_sharing_app.dto.rentalDto.RentalResponseDto;
import mate.academy.car_sharing_app.dto.rentalDto.RentalCarResponseDto;
import mate.academy.car_sharing_app.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RentalMapper {

    @Mapping(target = "rentalDate", ignore = true)
    @Mapping(target = "actualReturnDate", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "user", ignore = true)
    Rental toEntity(RentalRequestDto dto);

    @Mapping(target = "car", source = "car")
    @Mapping(target = "userId", source = "user.id")
    RentalResponseDto toDto(Rental rental);

    RentalCarResponseDto toRentalCarResponseDto(mate.academy.car_sharing_app.model.Car car);
}
