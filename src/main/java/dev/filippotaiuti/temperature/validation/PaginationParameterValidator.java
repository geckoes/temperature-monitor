package dev.filippotaiuti.temperature.validation;

import org.springframework.stereotype.Component;

import dev.filippotaiuti.temperature.exception.InvalidPaginationParameterException;
import dev.filippotaiuti.temperature.exception.PageSizeTooLargeException;


@Component
public class PaginationParameterValidator {

    private static final int MAX_PAGE_SIZE = 1000;

    public void validate(String page, String size) {
        Integer pageNumber = validateIntegerParameter("page", page);
        Integer sizeNumber = validateIntegerParameter("size", size);

        if (pageNumber != null && pageNumber < 0) {
            throw new InvalidPaginationParameterException("Invalid value for parameter 'page'");
        }
        if (sizeNumber != null && sizeNumber <= 0) {
            throw new InvalidPaginationParameterException("Invalid value for parameter 'size'");
        }
        if (sizeNumber != null && sizeNumber > MAX_PAGE_SIZE) {
            throw new PageSizeTooLargeException(MAX_PAGE_SIZE);
        }
    }

    private Integer validateIntegerParameter(String name, String value) {
        if (value == null) {
            return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidPaginationParameterException("Invalid value for parameter '" + name + "'", e);
        }
    }

}
