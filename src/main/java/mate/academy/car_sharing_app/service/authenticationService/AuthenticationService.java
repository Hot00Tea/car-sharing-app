package mate.academy.car_sharing_app.service.authenticationService;

import mate.academy.car_sharing_app.dto.userDto.UserLoginRequestDto;
import mate.academy.car_sharing_app.dto.userDto.UserLoginResponseDto;

public interface AuthenticationService {

    UserLoginResponseDto authenticate(UserLoginRequestDto request);
}
