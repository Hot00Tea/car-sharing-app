package mate.academy.car_sharing_app.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import mate.academy.car_sharing_app.dto.rentalDto.RentalRequestDto;
import mate.academy.car_sharing_app.dto.rentalDto.RentalResponseDto;
import mate.academy.car_sharing_app.dto.rentalDto.RentalCarResponseDto;
import mate.academy.car_sharing_app.model.car.Car;
import mate.academy.car_sharing_app.model.rental.Rental;
import mate.academy.car_sharing_app.model.user.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class RentalMapperTest {

    private final RentalMapper rentalMapper =
            Mappers.getMapper(RentalMapper.class);

    @Test
    void toEntity_shouldMapRequestFieldsAndIgnoreOtherFields() {
        RentalRequestDto requestDto = new RentalRequestDto();

        Rental rental = rentalMapper.toEntity(requestDto);

        assertNull(rental.getId());
        assertNull(rental.getRentalDate());
        assertNull(rental.getActualReturnDate());
        assertNull(rental.getCar());
        assertNull(rental.getUser());
    }

    @Test
    void toDto_shouldMapRentalAndUserId() {
        User user = new User();
        user.setId(10L);

        Car car = new Car();
        car.setId(5L);

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUser(user);
        rental.setCar(car);

        RentalResponseDto responseDto = rentalMapper.toDto(rental);

        assertEquals(1L, responseDto.getId());
        assertEquals(10L, responseDto.getUserId());
        assertEquals(car.getId(), responseDto.getCar().getId());
    }

    @Test
    void toRentalCarResponseDto_shouldMapCarFields() {
        Car car = new Car();
        car.setId(5L);

        RentalCarResponseDto responseDto =
                rentalMapper.toRentalCarResponseDto(car);

        assertEquals(5L, responseDto.getId());
    }
}
