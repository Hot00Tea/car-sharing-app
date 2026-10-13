package mate.academy.car_sharing_app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import mate.academy.car_sharing_app.dto.userDto.UserResponseDto;
import mate.academy.car_sharing_app.dto.userDto.UserRoleUpdateRequestDto;
import mate.academy.car_sharing_app.dto.userDto.UserUpdateRequestDto;
import mate.academy.car_sharing_app.service.userService.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    @Test
    void getUser_shouldReturnCurrentUser() {
        String email = "test@example.com";
        UserResponseDto expectedDto = new UserResponseDto();

        when(authentication.getName()).thenReturn(email);
        when(userService.getCurrentUser(email)).thenReturn(expectedDto);

        UserResponseDto actualDto = userController.getUser(authentication);

        assertEquals(expectedDto, actualDto);
        verify(userService).getCurrentUser(email);
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() {
        String email = "test@example.com";
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto();
        UserResponseDto expectedDto = new UserResponseDto();

        when(authentication.getName()).thenReturn(email);
        when(userService.updateUser(email, requestDto)).thenReturn(expectedDto);

        UserResponseDto actualDto =
                userController.updateUser(authentication, requestDto);

        assertEquals(expectedDto, actualDto);
        verify(userService).updateUser(email, requestDto);
    }

    @Test
    void updateUserRole_shouldReturnUpdatedUserRole() {
        Long userId = 1L;
        UserRoleUpdateRequestDto requestDto = new UserRoleUpdateRequestDto();
        UserResponseDto expectedDto = new UserResponseDto();

        when(userService.updateUserRole(userId, requestDto))
                .thenReturn(expectedDto);

        UserResponseDto actualDto =
                userController.updateUserRole(userId, requestDto);

        assertEquals(expectedDto, actualDto);
        verify(userService).updateUserRole(userId, requestDto);
    }
}
