package com.donorbox.backend.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for handling date and time operations with timezone support
 */
public class DateTimeUtil {
    
    private static final ZoneId KOLKATA_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter EMAIL_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
    private static final DateTimeFormatter FILE_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * Gets current date and time in Asia/Kolkata timezone
     * 
     * @return LocalDateTime in Asia/Kolkata timezone
     */
    public static LocalDateTime getCurrentKolkataTime() {
        return LocalDateTime.now(KOLKATA_ZONE);
    }
    
    /**
     * Converts a LocalDateTime to Asia/Kolkata timezone and formats it for email display
     * 
     * @param dateTime the LocalDateTime to convert
     * @return formatted date string in Asia/Kolkata timezone
     */
    public static String formatForEmail(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        
        return dateTime
            .atZone(ZoneId.systemDefault())
            .withZoneSameInstant(KOLKATA_ZONE)
            .format(EMAIL_DATE_FORMATTER);
    }
    
    /**
     * Gets current date and time in Asia/Kolkata timezone formatted for email display
     * 
     * @return formatted current date string in Asia/Kolkata timezone
     */
    public static String getCurrentTimeForEmail() {
        return LocalDateTime.now()
            .atZone(ZoneId.systemDefault())
            .withZoneSameInstant(KOLKATA_ZONE)
            .format(EMAIL_DATE_FORMATTER);
    }
    
    /**
     * Converts a LocalDateTime to Asia/Kolkata timezone and formats it for display
     * 
     * @param dateTime the LocalDateTime to convert
     * @return formatted date string in Asia/Kolkata timezone for display
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        
        return dateTime
            .atZone(ZoneId.systemDefault())
            .withZoneSameInstant(KOLKATA_ZONE)
            .format(DISPLAY_DATE_FORMATTER);
    }
    
    /**
     * Gets current date and time in Asia/Kolkata timezone formatted for file naming
     * 
     * @return formatted current date string in Asia/Kolkata timezone for file naming
     */
    public static String getCurrentTimeForFileNaming() {
        return LocalDateTime.now(KOLKATA_ZONE).format(FILE_TIMESTAMP_FORMATTER);
    }
    
    /**
     * Converts a LocalDateTime to Asia/Kolkata timezone
     * 
     * @param dateTime the LocalDateTime to convert
     * @return LocalDateTime in Asia/Kolkata timezone
     */
    public static LocalDateTime toKolkataTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        
        return dateTime
            .atZone(ZoneId.systemDefault())
            .withZoneSameInstant(KOLKATA_ZONE)
            .toLocalDateTime();
    }
    
    /**
     * Gets current time in Asia/Kolkata timezone for database storage
     * This should be used in @PrePersist and @PreUpdate methods
     * 
     * @return LocalDateTime in Asia/Kolkata timezone
     */
    public static LocalDateTime getCurrentTimeForDatabase() {
        return LocalDateTime.now(KOLKATA_ZONE);
    }
}
