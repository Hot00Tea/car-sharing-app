package mate.academy.car_sharing_app.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequestDto {

    @Email(message = "Email is not valid")
    private String email;

    private String firstName;

    private String lastName;
}
