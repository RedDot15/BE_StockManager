package org.reddot15.be_stockmanager.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeValidator {
    public static String validate(String dateTimeString) {
        // Null exception
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        try {
            // Validate data input
            LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);
            // Return original
            return dateTimeString;
        } catch (DateTimeParseException e) {
            throw new DateTimeParseException("Invalid date/time format for: " + dateTimeString + ". Expected ISO 8601 format (e.g., 'yyyy-MM-ddTHH:mm:ssZ').", dateTimeString, e.getErrorIndex());
        }
    }
}
