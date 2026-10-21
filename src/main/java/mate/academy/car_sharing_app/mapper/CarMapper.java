package mate.academy.car_sharing_app.mapper;

import mate.academy.car_sharing_app.dto.carDto.CarRequestDto;
import mate.academy.car_sharing_app.dto.carDto.CarResponseDto;
import mate.academy.car_sharing_app.model.car.Car;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CarMapper {

    @Mapping(target = "id", ignore = true)
    Car toEntity(CarRequestDto dto);

    @Mapping(target = "id", ignore = true)
    void updateFromDto(CarRequestDto dto, @MappingTarget Car car);

    CarResponseDto toDto(Car car);
}
