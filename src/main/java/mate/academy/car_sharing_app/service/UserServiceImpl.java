package mate.academy.car_sharing_app.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mate.academy.car_sharing_app.dto.userDto.UserRegistrationRequestDto;
import mate.academy.car_sharing_app.dto.userDto.UserResponseDto;
import mate.academy.car_sharing_app.dto.userDto.UserRoleUpdateRequestDto;
import mate.academy.car_sharing_app.dto.userDto.UserUpdateRequestDto;
import mate.academy.car_sharing_app.exception.RegistrationException;
import mate.academy.car_sharing_app.exception.UserException;
import mate.academy.car_sharing_app.mapper.UserMapper;
import mate.academy.car_sharing_app.model.Role;
import mate.academy.car_sharing_app.model.User;
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
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RegistrationException(
                    "User with email " + request.getEmail() + " already exists"
            );
        }

        if (!request.getPassword().equals(request.getRepeatPassword())) {
            throw new RegistrationException("Passwords do not match");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.CUSTOMER);

        user = userRepository.save(user);

        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("Can't find user by: " + email));
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto updateUser(String email, UserUpdateRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(
                        "Can't find user by: " + email));

        if (StringUtils.hasText(requestDto.getFirstName())) {
            user.setFirstName(requestDto.getFirstName());
        }

        if (StringUtils.hasText(requestDto.getLastName())) {
            user.setLastName(requestDto.getLastName());
        }

        if (StringUtils.hasText(requestDto.getEmail())) {
            String newEmail = requestDto.getEmail().toLowerCase();

            if (!newEmail.equals(user.getEmail())) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new RegistrationException("Email already used");
                }

                user.setEmail(newEmail);
            }
        }

        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto updateUserRole(Long id,
                                          UserRoleUpdateRequestDto requestDto) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new UserException("Can't find user by: " + id));

        user.setRole(requestDto.getRole());
        userRepository.save(user);

        return userMapper.toDto(user);
    }
}
