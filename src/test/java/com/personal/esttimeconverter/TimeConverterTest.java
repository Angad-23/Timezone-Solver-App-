package com.personal.esttimeconverter;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeConverterTest {

    @Test
    void shiftsStartAndEndTimeForwardByOffset() {
        LocalDateTime est = LocalDateTime.of(2025, 5, 12, 9, 0);

        TimeConverter.ConvertedTimes result = TimeConverter.convert(est, 4, 30);

        assertEquals("2025-05-12 13:00:00", result.lessonStartTime());
        assertEquals("2025-05-12 13:30:00", result.lessonEndTime());
    }

    @Test
    void rollsOverToNextDayWhenOffsetCrossesMidnight() {
        LocalDateTime est = LocalDateTime.of(2025, 5, 12, 21, 0);

        TimeConverter.ConvertedTimes result = TimeConverter.convert(est, 4, 60);

        assertEquals("2025-05-13 01:00:00", result.lessonStartTime());
        assertEquals("2025-05-13 02:00:00", result.lessonEndTime());
    }
}
