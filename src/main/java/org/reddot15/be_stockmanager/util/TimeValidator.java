package org.reddot15.be_stockmanager.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeValidator {
    public static String validateDateTime(String dateTimeString) {
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

    public static String validateDate(String date) {
        // Null exception
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        try {
            // Validate data input
            LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            // Return original
            return date;
        } catch (DateTimeParseException e) {
            throw new DateTimeParseException("Invalid date/time format for: " + date + ". Expected ISO 8601 format (e.g., 'yyyy-MM-dd').", date, e.getErrorIndex());
        }
    }
}
