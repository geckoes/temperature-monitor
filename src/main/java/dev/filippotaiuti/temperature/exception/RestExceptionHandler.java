package dev.filippotaiuti.temperature.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
public class RestExceptionHandler {

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
	public ProblemDetail handlePageSizeTooLarge(UnsupportedSortPropertyException ex) {
		return buildBadRequest(ex,  "UNSUPPORTED_SORT_PROPERTY");
	}

	private ProblemDetail buildBadRequest(RuntimeException ex, String code) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problem.setProperty("code", code);
		return problem;
	}

}
