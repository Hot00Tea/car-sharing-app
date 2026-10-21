package mate.academy.car_sharing_app.service.userService;

import mate.academy.car_sharing_app.dto.userDto.UserRegistrationRequestDto;
import mate.academy.car_sharing_app.dto.userDto.UserResponseDto;
import mate.academy.car_sharing_app.dto.userDto.UserRoleUpdateRequestDto;
import mate.academy.car_sharing_app.dto.userDto.UserUpdateRequestDto;
import mate.academy.car_sharing_app.exception.RegistrationException;
import mate.academy.car_sharing_app.exception.EntityNotFoundException;
import mate.academy.car_sharing_app.mapper.UserMapper;
import mate.academy.car_sharing_app.model.user.Role;
import mate.academy.car_sharing_app.model.user.User;
import mate.academy.car_sharing_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String EMAIL = "user@test.com";
    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponseDto responseDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setEmail(EMAIL);
        user.setPassword("password123");
        user.setRole(Role.CUSTOMER);
        user.setFirstName("John");
        user.setLastName("Smith");

        responseDto = new UserResponseDto();
    }

    @Test
    void register_shouldEncodePasswordAndSaveCustomer() {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setEmail(EMAIL);
        request.setPassword("password123");
        request.setRepeatPassword("password123");

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.register(request);

        assertNotNull(result);
        assertEquals("encodedPassword", user.getPassword());
        assertEquals(Role.CUSTOMER, user.getRole());

        verify(userRepository).save(user);
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_shouldThrowWhenPasswordsDoNotMatch() {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setEmail(EMAIL);
        request.setPassword("password123");
        request.setRepeatPassword("differentPassword");

        assertThrows(
                RegistrationException.class,
                () -> userService.register(request)
        );

        verify(userRepository, never()).save(user);
        verify(userMapper, never()).toEntity(request);
    }

    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setEmail(EMAIL);
        request.setPassword("password123");
        request.setRepeatPassword("password123");

        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThrows(
                RegistrationException.class,
                () -> userService.register(request)
        );

        verify(userRepository, never()).save(user);
        verify(userMapper, never()).toEntity(request);
    }

    @Test
    void getCurrentUser_shouldReturnUserDto() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.getCurrentUser(EMAIL);

        assertNotNull(result);
        verify(userMapper).toDto(user);
    }

    @Test
    void getCurrentUser_shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> userService.getCurrentUser(EMAIL)
        );

        verify(userMapper, never()).toDto(user);
    }

    @Test
    void updateUser_shouldUpdateProvidedFields() {
        UserUpdateRequestDto request = new UserUpdateRequestDto();
        request.setFirstName("Mike");
        request.setLastName("Brown");
        request.setEmail("NEW@TEST.COM");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.updateUser(EMAIL, request);

        assertNotNull(result);
        assertEquals("new@test.com", user.getEmail());

        verify(userMapper).updateFromDto(request, user);
        verify(userMapper).toDto(user);
    }

    @Test
    void updateUser_shouldKeepFieldsWhenValuesAreBlank() {
        UserUpdateRequestDto request = new UserUpdateRequestDto();
        request.setFirstName(" ");
        request.setLastName("");
        request.setEmail("");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);

        userService.updateUser(EMAIL, request);

        assertEquals(EMAIL, user.getEmail());
        verify(userMapper).updateFromDto(request, user);
    }

    @Test
    void updateUser_shouldThrowWhenNewEmailAlreadyExists() {
        UserUpdateRequestDto request = new UserUpdateRequestDto();
        request.setEmail("other@test.com");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("other@test.com")).thenReturn(true);

        assertThrows(
                RegistrationException.class,
                () -> userService.updateUser(EMAIL, request)
        );

        assertEquals(EMAIL, user.getEmail());
    }

    @Test
    void updateUserRole_shouldChangeUserRole() {
        UserRoleUpdateRequestDto request = new UserRoleUpdateRequestDto();
        request.setRole(Role.MANAGER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.updateUserRole(USER_ID, request);

        assertNotNull(result);
        verify(userMapper).updateRoleFromDto(request, user);
        verify(userMapper).toDto(user);
    }

    @Test
    void updateUserRole_shouldThrowWhenUserDoesNotExist() {
        UserRoleUpdateRequestDto request = new UserRoleUpdateRequestDto();
        request.setRole(Role.MANAGER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUserRole(USER_ID, request)
        );

        verify(userMapper, never()).toDto(user);
    }
}
