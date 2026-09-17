package mate.academy.car_sharing_app.mapper;

import mate.academy.car_sharing_app.dto.UserRegistrationRequestDto;
import mate.academy.car_sharing_app.dto.UserResponseDto;
import mate.academy.car_sharing_app.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRegistrationRequestDto dto);

    UserResponseDto toDto(User user);
}
