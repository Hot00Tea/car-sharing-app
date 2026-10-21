package mate.academy.car_sharing_app.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(
            String entityName,
            String identifierName,
            Object identifier) {
        super("%s with %s '%s' was not found"
                .formatted(entityName, identifierName, identifier));
    }
}
