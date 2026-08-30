package com.personal.esttimeconverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses lines of the form "yyyy-MM-dd HH:mm,durationMinutes" (duration optional,
 * defaults to 30) and converts each to the platform upload format.
 */
public final class BulkConverter {

    private static final DateTimeFormatter INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private BulkConverter() {
    }

    public static List<BulkRow> convertLines(String rawInput, int offsetHours, int defaultDurationMinutes) {
        List<BulkRow> rows = new ArrayList<>();
        if (rawInput == null || rawInput.isBlank()) {
            return rows;
        }

        for (String rawLine : rawInput.split("\\r?\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            rows.add(convertLine(line, offsetHours, defaultDurationMinutes));
        }
        return rows;
    }

    private static BulkRow convertLine(String line, int offsetHours, int defaultDurationMinutes) {
        String[] parts = line.split(",");
        String dateTimePart = parts[0].trim();
        int durationMinutes = defaultDurationMinutes;

        if (parts.length > 1) {
            try {
                durationMinutes = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                return new BulkRow(line, null, null,
                        "Could not read duration '" + parts[1].trim() + "' as a number");
            }
        }

        try {
            LocalDateTime estDateTime = LocalDateTime.parse(dateTimePart, INPUT_FORMAT);
            TimeConverter.ConvertedTimes converted = TimeConverter.convert(estDateTime, offsetHours, durationMinutes);
            return new BulkRow(line, converted.lessonStartTime(), converted.lessonEndTime(), null);
        } catch (DateTimeParseException e) {
            return new BulkRow(line, null, null,
                    "Could not read '" + dateTimePart + "' — expected format yyyy-MM-dd HH:mm");
        }
    }
}
