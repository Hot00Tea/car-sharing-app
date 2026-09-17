package mate.academy.car_sharing_app.service;

import mate.academy.car_sharing_app.dto.UserRegistrationRequestDto;
import mate.academy.car_sharing_app.dto.UserResponseDto;
import mate.academy.car_sharing_app.dto.UserRoleUpdateRequestDto;
import mate.academy.car_sharing_app.dto.UserUpdateRequestDto;

public interface UserService {

    UserResponseDto register(UserRegistrationRequestDto request);

    UserResponseDto getCurrentUser(String email);

    UserResponseDto updateUser(String email, UserUpdateRequestDto requestDto);

    UserResponseDto updateUserRole(Long id,
                                            UserRoleUpdateRequestDto requestDto);
}
