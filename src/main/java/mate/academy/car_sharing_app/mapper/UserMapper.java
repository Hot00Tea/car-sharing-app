package mate.academy.car_sharing_app.mapper;

import mate.academy.car_sharing_app.dto.userDto.UserRegistrationRequestDto;
import mate.academy.car_sharing_app.dto.userDto.UserRoleUpdateRequestDto;
import mate.academy.car_sharing_app.dto.userDto.UserResponseDto;
import mate.academy.car_sharing_app.dto.userDto.UserUpdateRequestDto;
import mate.academy.car_sharing_app.model.user.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Condition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.util.StringUtils;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toEntity(UserRegistrationRequestDto dto);

    UserResponseDto toDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateFromDto(UserUpdateRequestDto dto, @MappingTarget User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateRoleFromDto(UserRoleUpdateRequestDto dto, @MappingTarget User user);

    @Condition
    default boolean hasText(String value) {
        return StringUtils.hasText(value);
    }
}
