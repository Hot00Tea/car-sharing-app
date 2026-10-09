package mate.academy.car_sharing_app.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.car_sharing_app.dto.rentalDto.RentalRequestDto;
import mate.academy.car_sharing_app.dto.rentalDto.RentalResponseDto;
import mate.academy.car_sharing_app.service.rentalService.RentalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rentals")
public class RentalController {

    private final RentalService rentalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new rental", description = "Create a new rental")
    public RentalResponseDto createRental(
            Authentication authentication,
            @Valid @RequestBody RentalRequestDto requestDto) {
        return rentalService.createRental(
                authentication.getName(),
                requestDto
        );
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Return rental", description = "Return rental")
    public RentalResponseDto returnRental(
            Authentication authentication,
            @PathVariable Long id) {
        return rentalService.returnRental(
                authentication.getName(),
                id
        );
    }

    @GetMapping
    @Operation(summary = "Get all rentals", description = "Get all rentals")
    public Page<RentalResponseDto> findAllRentals(
            Authentication authentication,
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam(value = "is_active", required = false) Boolean isActive,
            Pageable pageable) {
        return rentalService.getAll(
                authentication.getName(),
                userId,
                isActive,
                pageable
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get rental by id", description = "Get rental by id")
    public RentalResponseDto findById(
            Authentication authentication,
            @PathVariable Long id) {
        return rentalService.getById(
                authentication.getName(),
                id
        );
    }
}
