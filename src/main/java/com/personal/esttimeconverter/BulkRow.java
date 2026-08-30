package com.personal.esttimeconverter;

public record BulkRow(String inputLine, String lessonStartTime, String lessonEndTime, String error) {

    public boolean hasError() {
        return error != null && !error.isBlank();
    }
}
