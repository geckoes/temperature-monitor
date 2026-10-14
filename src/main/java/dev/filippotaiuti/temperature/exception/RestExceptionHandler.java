package dev.filippotaiuti.temperature.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import dev.filippotaiuti.temperature.dto.ValidationError;

@RestControllerAdvice 
public class RestExceptionHandler {

    @ExceptionHandler(InvalidPaginationParameterException.class)
    public ProblemDetail handleInvalidPaginationParameter(InvalidPaginationParameterException ex) {
        return buildBadRequest(ex,  "INVALID_PARAMETER");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Invalid value for parameter '" + ex.getName() + "'");
        problem.setProperty("code", "INVALID_PARAMETER");
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Request body is malformed or contains invalid values"
        );
        problem.setProperty("code", "INVALID_REQUEST_BODY");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setProperty("code", "VALIDATION_FAILED");
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
                .toList();
        problem.setProperty("errors", errors);
        return problem;
    }

	@ExceptionHandler(InvalidTimeRangeException.class)
	public ProblemDetail handleInvalidTimeRange(InvalidTimeRangeException ex) {
		return buildBadRequest(ex, "INVALID_TIME_RANGE");
	}

	@ExceptionHandler(IncompleteTimeRangeException.class)
	public ProblemDetail handleIncompleteTimeRange(IncompleteTimeRangeException ex) {
		return buildBadRequest(ex,  "INCOMPLETE_TIME_RANGE");
	}

	@ExceptionHandler(SensorIdRequiredException.class)
	public ProblemDetail handleSensorIdRequired(SensorIdRequiredException ex) {
		return buildBadRequest(ex,  "SENSOR_ID_REQUIRED");
	}

	@ExceptionHandler(PageSizeTooLargeException.class)
	public ProblemDetail handlePageSizeTooLarge(PageSizeTooLargeException ex) {
		return buildBadRequest(ex,  "PAGE_SIZE_TOO_LARGE");
	}

	@ExceptionHandler(UnsupportedSortPropertyException.class)
	public ProblemDetail handleUnsupportedSortProperty(UnsupportedSortPropertyException ex) {
		return buildBadRequest(ex,  "UNSUPPORTED_SORT_PROPERTY");
	}

	private ProblemDetail buildBadRequest(RuntimeException ex, String code) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problem.setProperty("code", code);
		return problem;
	}

}
