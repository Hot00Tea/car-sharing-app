package mate.academy.car_sharing_app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.car_sharing_app.dto.UserLoginRequestDto;
import mate.academy.car_sharing_app.dto.UserLoginResponseDto;
import mate.academy.car_sharing_app.dto.UserRegistrationRequestDto;
import mate.academy.car_sharing_app.dto.UserResponseDto;
import mate.academy.car_sharing_app.service.AuthenticationService;
import mate.academy.car_sharing_app.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public UserResponseDto register(@Valid @RequestBody UserRegistrationRequestDto request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public UserLoginResponseDto login(@Valid @RequestBody UserLoginRequestDto requestDto) {
        return authenticationService.authenticate(requestDto);
    }
}
