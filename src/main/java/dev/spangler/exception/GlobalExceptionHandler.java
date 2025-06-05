package dev.spangler.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.postgresql.util.PSQLException;

import java.util.HashMap;
import java.util.Map;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        Map<String, Object> error = new HashMap<>();

        // 1. Jakarta Validation
        ConstraintViolationException beanValidationEx = findCause(exception, ConstraintViolationException.class);

        if (beanValidationEx != null) {
            error.put("error", "Validation failed");
            error.put("details", beanValidationEx.getConstraintViolations()
                    .stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .toList());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // 2. PostgreSQL uniqueness error
        PSQLException psqlEx = findCause(exception, PSQLException.class);

        if (psqlEx != null && psqlEx.getMessage() != null && psqlEx.getMessage().contains("duplicate key value")) {
            error = getError(psqlEx);

            return Response.status(Response.Status.CONFLICT)
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // 3. NotFoundException
        NotFoundException notFound = findCause(exception, NotFoundException.class);

        if (notFound != null) {
            error.put("error", notFound.getMessage());

            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // 4. Fallback
        error.put("error", "Internal server error");
        error.put("message", exception.getMessage());

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private static Map<String, Object> getError(PSQLException psqlEx) {
        String message = "Duplicate value violates a unique constraint.";
        String constraintName = null;

        if (psqlEx.getServerErrorMessage() != null) {
            constraintName = psqlEx.getServerErrorMessage().getConstraint();
        }

        if (constraintName != null && constraintName.contains("email")) {
            message = "Email must be unique.";
        }

        if (constraintName != null && constraintName.contains("mobile")) {
            message = "Mobile number must be unique.";
        }

        return Map.of("error", message);
    }

    private <T extends Throwable> T findCause(Throwable throwable, Class<T> type) {
        while (throwable != null) {
            if (type.isInstance(throwable)) {
                return type.cast(throwable);
            }

            throwable = throwable.getCause();
        }

        return null;
    }

}
