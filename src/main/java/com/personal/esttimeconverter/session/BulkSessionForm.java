package com.personal.esttimeconverter.session;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BulkSessionForm {

    private List<PairEntry> pairs = new ArrayList<>();

    @NotNull(message = "Pick a lesson date")
    private LocalDate date;

    @NotNull(message = "Pick a start time")
    private LocalTime startTime;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private int durationMinutes = 30;

    @Min(value = -12)
    private int offsetHours = 4;

    public List<PairEntry> getPairs() {
        return pairs;
    }

    public void setPairs(List<PairEntry> pairs) {
        this.pairs = pairs;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getOffsetHours() {
        return offsetHours;
    }

    public void setOffsetHours(int offsetHours) {
        this.offsetHours = offsetHours;
    }
}