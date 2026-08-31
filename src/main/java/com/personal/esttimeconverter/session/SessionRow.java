package com.personal.esttimeconverter.session;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "session_rows")
public class SessionRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String learnerEmail;
    private String learnerName;
    private String tutorEmail;
    private String tutorName;
    private String lessonStartTime;
    private String lessonEndTime;
    private String teachingSubjectIdentifier;

    public SessionRow() {
    }

    public SessionRow(String learnerEmail, String learnerName, String tutorEmail, String tutorName,
                       String lessonStartTime, String lessonEndTime, String teachingSubjectIdentifier) {
        this.learnerEmail = learnerEmail;
        this.learnerName = learnerName;
        this.tutorEmail = tutorEmail;
        this.tutorName = tutorName;
        this.lessonStartTime = lessonStartTime;
        this.lessonEndTime = lessonEndTime;
        this.teachingSubjectIdentifier = teachingSubjectIdentifier;
    }

    public Long getId() {
        return id;
    }

    public String getLearnerEmail() {
        return learnerEmail;
    }

    public String getLearnerName() {
        return learnerName;
    }

    public String getTutorEmail() {
        return tutorEmail;
    }

    public String getTutorName() {
        return tutorName;
    }

    public String getLessonStartTime() {
        return lessonStartTime;
    }

    public String getLessonEndTime() {
        return lessonEndTime;
    }

    public String getTeachingSubjectIdentifier() {
        return teachingSubjectIdentifier;
    }
}
