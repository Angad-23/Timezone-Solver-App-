package com.personal.esttimeconverter.session;

public class PairEntry {

    private String learnerEmail;
    private String tutorEmail;
    private String subject;

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

    public boolean isBlank() {
        return (learnerEmail == null || learnerEmail.isBlank())
                && (tutorEmail == null || tutorEmail.isBlank())
                && (subject == null || subject.isBlank());
    }
}