package mate.academy.car_sharing_app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import mate.academy.car_sharing_app.dto.rentalDto.RentalRequestDto;
import mate.academy.car_sharing_app.dto.rentalDto.RentalResponseDto;
import mate.academy.car_sharing_app.service.rentalService.RentalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class RentalControllerTest {

    @Mock
    private RentalService rentalService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RentalController rentalController;

    @Test
    void createRental_shouldReturnCreatedRental() {
        String email = "test@example.com";
        RentalRequestDto requestDto = new RentalRequestDto();
        RentalResponseDto expectedDto = new RentalResponseDto();

        when(authentication.getName()).thenReturn(email);
        when(rentalService.createRental(email, requestDto)).thenReturn(expectedDto);

        RentalResponseDto actualDto =
                rentalController.createRental(authentication, requestDto);

        assertEquals(expectedDto, actualDto);
        verify(rentalService).createRental(email, requestDto);
    }

    @Test
    void returnRental_shouldReturnRental() {
        String email = "test@example.com";
        Long rentalId = 1L;
        RentalResponseDto expectedDto = new RentalResponseDto();

        when(authentication.getName()).thenReturn(email);
        when(rentalService.returnRental(email, rentalId)).thenReturn(expectedDto);

        RentalResponseDto actualDto =
                rentalController.returnRental(authentication, rentalId);

        assertEquals(expectedDto, actualDto);
        verify(rentalService).returnRental(email, rentalId);
    }

    @Test
    void findAllRentals_shouldReturnPageOfRentals() {
        String email = "test@example.com";
        Long userId = 1L;
        Boolean isActive = true;
        Pageable pageable = PageRequest.of(0, 10);
        Page<RentalResponseDto> expectedPage = new PageImpl<>(List.of());

        when(authentication.getName()).thenReturn(email);
        when(rentalService.getAll(email, userId, isActive, pageable))
                .thenReturn(expectedPage);

        Page<RentalResponseDto> actualPage =
                rentalController.findAllRentals(
                        authentication, userId, isActive, pageable);

        assertEquals(expectedPage, actualPage);
        verify(rentalService).getAll(email, userId, isActive, pageable);
    }

    @Test
    void findById_shouldReturnRental() {
        String email = "test@example.com";
        Long rentalId = 1L;
        RentalResponseDto expectedDto = new RentalResponseDto();

        when(authentication.getName()).thenReturn(email);
        when(rentalService.getById(email, rentalId)).thenReturn(expectedDto);

        RentalResponseDto actualDto =
                rentalController.findById(authentication, rentalId);

        assertEquals(expectedDto, actualDto);
        verify(rentalService).getById(email, rentalId);
    }
}
