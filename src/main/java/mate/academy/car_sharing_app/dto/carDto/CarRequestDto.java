package mate.academy.car_sharing_app.dto.carDto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import mate.academy.car_sharing_app.model.car.CarType;
import java.math.BigDecimal;

@Getter
@Setter
public class CarRequestDto {

    @NotBlank
    @Size(max = 255)
    private String model;

    @NotBlank
    @Size(max = 255)
    private String brand;

    @NotNull
    private CarType type;

    @NotNull
    @PositiveOrZero
    private Integer inventory;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal dailyFee;
}
