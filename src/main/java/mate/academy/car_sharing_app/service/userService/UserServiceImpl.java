package mate.academy.car_sharing_app.service.userService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto request) {
        validateRegistration(request);
        checkEmailAvailability(request.getEmail());

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.CUSTOMER);

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto getCurrentUser(String email) {
        return userMapper.toDto(getUserByEmail(email));
    }

    @Override
    public UserResponseDto updateUser(
            String email,
            UserUpdateRequestDto requestDto) {
        User user = getUserByEmail(email);

        if (StringUtils.hasText(requestDto.getEmail())
                && !requestDto.getEmail().equalsIgnoreCase(user.getEmail())) {
            checkEmailAvailability(requestDto.getEmail().toLowerCase());
        }
        userMapper.updateFromDto(requestDto, user);
        if (StringUtils.hasText(requestDto.getEmail())) {
            user.setEmail(requestDto.getEmail().toLowerCase());
        }

        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto updateUserRole(
            Long id,
            UserRoleUpdateRequestDto requestDto) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("User", "id", id)
        );

        userMapper.updateRoleFromDto(requestDto, user);

        return userMapper.toDto(user);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("User", "email", email)
        );
    }

    private void validateRegistration(UserRegistrationRequestDto request) {
        if (!request.getPassword().equals(request.getRepeatPassword())) {
            throw new RegistrationException(
                    "Passwords do not match for email '%s'"
                            .formatted(request.getEmail())
            );
        }
    }

    private void checkEmailAvailability(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RegistrationException(
                    "User with email '%s' already exists".formatted(email)
            );
        }
    }
}
