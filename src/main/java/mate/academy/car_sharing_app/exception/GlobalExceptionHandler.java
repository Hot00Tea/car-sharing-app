package mate.academy.car_sharing_app.exception;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CarException.class)
  public ResponseEntity<Map<String, String>> handleCarException(
          CarException exception) {
    return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(UserException.class)
  public ResponseEntity<Map<String, String>> handleUserException(
          UserException exception) {
    return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(RentalException.class)
  public ResponseEntity<Map<String, String>> handleRentalException(
          RentalException exception) {
    return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(PaymentException.class)
  public ResponseEntity<Map<String, String>> handlePaymentException(
          PaymentException exception) {
    return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(RegistrationException.class)
  public ResponseEntity<Map<String, String>> handleRegistrationException(
          RegistrationException exception) {
    return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationException(
          MethodArgumentNotValidException exception) {
    String message = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));

    return buildResponse(HttpStatus.BAD_REQUEST, message);
  }

  private ResponseEntity<Map<String, String>> buildResponse(
          HttpStatus status,
          String message) {
    return ResponseEntity.status(status)
            .body(Map.of(
                    "status", String.valueOf(status.value()),
                    "error", status.getReasonPhrase(),
                    "message", message
            ));
  }
}
