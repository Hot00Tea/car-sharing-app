package mate.academy.car_sharing_app.service;

import mate.academy.car_sharing_app.dto.userDto.UserRegistrationRequestDto;
import mate.academy.car_sharing_app.dto.userDto.UserResponseDto;
import mate.academy.car_sharing_app.dto.userDto.UserRoleUpdateRequestDto;
import mate.academy.car_sharing_app.dto.userDto.UserUpdateRequestDto;

public interface UserService {

    UserResponseDto register(UserRegistrationRequestDto request);

    UserResponseDto getCurrentUser(String email);

    UserResponseDto updateUser(String email, UserUpdateRequestDto requestDto);

    UserResponseDto updateUserRole(Long id,
                                            UserRoleUpdateRequestDto requestDto);
}
