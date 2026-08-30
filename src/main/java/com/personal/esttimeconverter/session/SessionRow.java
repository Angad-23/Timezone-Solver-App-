package com.personal.esttimeconverter.session;

public record SessionRow(
        String learnerEmail,
        String learnerName,
        String tutorEmail,
        String tutorName,
        String lessonStartTime,
        String lessonEndTime,
        String teachingSubjectIdentifier
) {
}
