package mate.academy.car_sharing_app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.car_sharing_app.dto.userDto.UserResponseDto;
import mate.academy.car_sharing_app.dto.userDto.UserRoleUpdateRequestDto;
import mate.academy.car_sharing_app.dto.userDto.UserUpdateRequestDto;
import mate.academy.car_sharing_app.service.userService.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponseDto getUser(Authentication authentication) {
        return userService.getCurrentUser(authentication.getName());
    }

    @PatchMapping("/me")
    public UserResponseDto updateUser(Authentication authentication,
                                      @Valid @RequestBody UserUpdateRequestDto requestDto) {
        return userService.updateUser(authentication.getName(), requestDto);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('MANAGER')")
    public UserResponseDto updateUserRole(@PathVariable("id") Long id,
                                          @Valid @RequestBody UserRoleUpdateRequestDto requestDto){

        return userService.updateUserRole(id, requestDto);
    }
}
