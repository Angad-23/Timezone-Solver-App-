package com.personal.esttimeconverter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Converts a date/time entered in India Standard Time into the real,
 * current US Eastern time — automatically switching between EST and EDT
 * depending on the date, since unlike the platform's flat-offset upload
 * format, real-world IST-to-Eastern gap actually changes with daylight
 * saving.
 */
public final class IstEstConverter {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final ZoneId EASTERN = ZoneId.of("America/New_York");
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ZONE_ABBREVIATION_FORMAT = DateTimeFormatter.ofPattern("zzz");

    private IstEstConverter() {
    }

    public static Result convert(LocalDateTime istDateTime) {
        ZonedDateTime istZoned = istDateTime.atZone(IST);
        ZonedDateTime easternZoned = istZoned.withZoneSameInstant(EASTERN);
        return new Result(
                easternZoned.format(OUTPUT_FORMAT),
                easternZoned.format(ZONE_ABBREVIATION_FORMAT)
        );
    }

    /**
     * @param easternDateTime   the converted date/time, e.g. "2026-08-31 04:15:00"
     * @param zoneAbbreviation  "EDT" or "EST" depending on whether daylight
     *                          saving is in effect on that date
     */
    public record Result(String easternDateTime, String zoneAbbreviation) {
    }
}