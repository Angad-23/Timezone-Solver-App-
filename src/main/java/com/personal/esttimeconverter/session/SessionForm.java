package com.personal.esttimeconverter.session;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class SessionForm {

    @NotBlank(message = "Pick a learner")
    private String learnerEmail;

    @NotBlank(message = "Pick a tutor")
    private String tutorEmail;

    @NotBlank(message = "Enter a subject")
    private String subject;

    @NotNull(message = "Pick a lesson date")
    private LocalDate date;

    @NotNull(message = "Pick a start time")
    private LocalTime startTime;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private int durationMinutes = 30;

    @Min(value = -12)
    private int offsetHours = 4;

    public String getLearnerEmail() {
        return learnerEmail;
    }

    public void setLearnerEmail(String learnerEmail) {
        this.learnerEmail = learnerEmail;
    }

    public String getTutorEmail() {
        return tutorEmail;
    }

    public void setTutorEmail(String tutorEmail) {
        this.tutorEmail = tutorEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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
