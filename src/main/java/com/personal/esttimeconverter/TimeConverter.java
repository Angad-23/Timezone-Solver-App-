package com.personal.esttimeconverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Converts a lesson time entered in EST into the string format the
 * tutoring platform expects on upload: "yyyy-MM-dd HH:mm:ss", shifted
 * forward by a fixed number of hours (the platform stores times ahead
 * of EST).
 */
public final class TimeConverter {

    public static final DateTimeFormatter UPLOAD_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TimeConverter() {
    }

    /**
     * @param estDateTime   the lesson time as entered in EST
     * @param offsetHours   hours to add to reach platform time (4 for EST during
     *                      Daylight Saving, 5 for Standard Time — see README)
     * @param durationMinutes lesson length in minutes
     * @return the platform-format start and end time strings
     */
    public static ConvertedTimes convert(LocalDateTime estDateTime, int offsetHours, int durationMinutes) {
        LocalDateTime platformStart = estDateTime.plusHours(offsetHours);
        LocalDateTime platformEnd = platformStart.plusMinutes(durationMinutes);
        return new ConvertedTimes(
                platformStart.format(UPLOAD_FORMAT),
                platformEnd.format(UPLOAD_FORMAT)
        );
    }

    public record ConvertedTimes(String lessonStartTime, String lessonEndTime) {
    }
}
