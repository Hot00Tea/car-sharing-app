package mate.academy.car_sharing_app.service;

import mate.academy.car_sharing_app.dto.UserLoginRequestDto;
import mate.academy.car_sharing_app.dto.UserLoginResponseDto;

public interface AuthenticationService {

    UserLoginResponseDto authenticate(UserLoginRequestDto request);
}
