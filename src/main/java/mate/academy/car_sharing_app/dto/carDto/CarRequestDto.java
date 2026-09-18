package mate.academy.car_sharing_app.dto.carDto;

import lombok.Getter;
import lombok.Setter;
import mate.academy.car_sharing_app.model.CarType;
import java.math.BigDecimal;

@Getter
@Setter
public class CarRequestDto {

    private String model;

    private String brand;

    private CarType type;

    private int inventory;

    private BigDecimal dailyFee;
}
