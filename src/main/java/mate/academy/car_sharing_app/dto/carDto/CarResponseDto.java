package mate.academy.car_sharing_app.dto.carDto;

import lombok.Data;
import mate.academy.car_sharing_app.model.CarType;
import java.math.BigDecimal;

@Data
public class CarResponseDto {

    private Long id;

    private String model;

    private String brand;

    private CarType type;

    private int inventory;

    private BigDecimal dailyFee;

}
