package mate.academy.car_sharing_app.mapper;

import mate.academy.car_sharing_app.dto.userDto.UserRegistrationRequestDto;
import mate.academy.car_sharing_app.dto.userDto.UserResponseDto;
import mate.academy.car_sharing_app.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toEntity(UserRegistrationRequestDto dto);

    UserResponseDto toDto(User user);
}
