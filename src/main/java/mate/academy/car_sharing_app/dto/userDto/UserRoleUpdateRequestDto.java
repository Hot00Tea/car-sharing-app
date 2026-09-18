package mate.academy.car_sharing_app.dto.userDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mate.academy.car_sharing_app.model.Role;

@Data
public class UserRoleUpdateRequestDto {

    @NotNull
    private Role role;
}
