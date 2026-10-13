package mate.academy.car_sharing_app.service.authenticationService;

import mate.academy.car_sharing_app.dto.userDto.UserLoginRequestDto;
import mate.academy.car_sharing_app.dto.userDto.UserLoginResponseDto;
import mate.academy.car_sharing_app.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    private static final String EMAIL = "user@test.com";
    private static final String PASSWORD = "password123";
    private static final String JWT_TOKEN = "test-jwt-token";

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void authenticate_shouldReturnJwtToken() {
        UserLoginRequestDto request = new UserLoginRequestDto();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD);

        when(authenticationManager.authenticate(authRequest))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn(EMAIL);
        when(jwtUtil.generateToken(EMAIL)).thenReturn(JWT_TOKEN);

        UserLoginResponseDto result = authenticationService.authenticate(request);

        assertEquals(JWT_TOKEN, result.token());

        verify(authenticationManager).authenticate(authRequest);
        verify(jwtUtil).generateToken(EMAIL);
    }

    @Test
    void authenticate_shouldThrowWhenCredentialsAreInvalid() {
        UserLoginRequestDto request = new UserLoginRequestDto();
        request.setEmail(EMAIL);
        request.setPassword("wrong-password");

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(
                        EMAIL,
                        "wrong-password"
                );

        when(authenticationManager.authenticate(authRequest))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.authenticate(request)
        );

        verify(jwtUtil, never()).generateToken(EMAIL);
    }

    @Test
    void authenticate_shouldUseAuthenticatedUsernameToGenerateToken() {
        UserLoginRequestDto request = new UserLoginRequestDto();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        Authentication authenticatedUser = org.mockito.Mockito.mock(
                Authentication.class
        );

        when(authenticationManager.authenticate(
                org.mockito.ArgumentMatchers.any(
                        UsernamePasswordAuthenticationToken.class
                )
        )).thenReturn(authenticatedUser);

        when(authenticatedUser.getName()).thenReturn("authenticated@test.com");
        when(jwtUtil.generateToken("authenticated@test.com"))
                .thenReturn(JWT_TOKEN);

        UserLoginResponseDto result = authenticationService.authenticate(request);

        assertEquals(JWT_TOKEN, result.token());
        verify(jwtUtil).generateToken("authenticated@test.com");
    }
}
