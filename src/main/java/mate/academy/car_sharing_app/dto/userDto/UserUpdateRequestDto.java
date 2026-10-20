package mate.academy.car_sharing_app.dto.userDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequestDto {

    @Email(message = "Email is not valid")
    @Size(max = 255)
    private String email;

    @Pattern(regexp = ".*\\S.*", message = "First name must not be blank")
    @Size(max = 100)
    private String firstName;

    @Pattern(regexp = ".*\\S.*", message = "Last name must not be blank")
    @Size(max = 100)
    private String lastName;
}
