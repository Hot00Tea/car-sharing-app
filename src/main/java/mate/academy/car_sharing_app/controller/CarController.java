package mate.academy.car_sharing_app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.car_sharing_app.dto.carDto.CarRequestDto;
import mate.academy.car_sharing_app.dto.carDto.CarResponseDto;
import mate.academy.car_sharing_app.service.CarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.DeleteMapping;


@Tag(name = "Car management", description = "Endpoint for managing cars")
@RequiredArgsConstructor
@RestController
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;

    @PreAuthorize("hasRole('CUSTOMER', 'MANAGER')")
    @GetMapping
    @Operation(summary = "Get all cars", description = "Get a list of all available cars")
    public Page<CarResponseDto> findAll(Pageable pageable) {
        return carService.getAll(pageable);
    }

    @PreAuthorize("hasRole('CUSTOMER', 'MANAGER')")
    @GetMapping("/{id}")
    public CarResponseDto findById(@PathVariable Long id) {
        return carService.findById(id);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new car", description = "Create a new car")
    public CarResponseDto createCar(@RequestBody @Valid CarRequestDto carRequestDto) {
        return carService.createCar(carRequestDto);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}")
    public CarResponseDto updateCar(@PathVariable Long id,
                                    @RequestBody @Valid CarRequestDto carRequestDto) {
        return carService.updateCar(id, carRequestDto);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        carService.deleteById(id);
    }
}
