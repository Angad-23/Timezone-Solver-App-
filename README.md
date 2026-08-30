# EST time converter

A tiny personal Spring Boot app that builds rows for a tutoring platform's
lesson upload sheet: pick a learner and tutor from a saved roster, set an
EST time, and get back a row with everything filled in — including the
platform's `lesson_starttime` / `lesson_endtime` format (EST + a fixed
offset, default 4 hours).

What's on the site:
- **Sessions page** — pick learner + tutor from dropdowns, set subject,
  date, EST start time, and duration. Each one adds a row to a running
  "upload sheet" you can download as an `.xlsx` matching the platform's
  columns (`learner_email`, `learner_name`, `tutor_email`, `tutor_name`,
  `lesson_starttime`, `lesson_endtime`, `teaching_subject_identifier`).
- **Roster page** — add learners/tutors one at a time, or import them in
  bulk from the file the platform exports for all its users (.csv, .xlsx,
  or .xls all work — it just needs a "name" column and an "email" column
  somewhere in the header row, exact header text doesn't matter).
- Two standalone time converters (single + bulk) also live on the Sessions
  page for quick one-off conversions that don't need the roster.

Your roster is saved to `data/roster.json` on disk so it survives restarts,
and is excluded from git (see `.gitignore`) since it's personal data.

## Run it locally

Requires Java 21 and Maven.

```bash
mvn spring-boot:run
```

Then open http://localhost:8080

## Build a runnable jar

```bash
mvn clean package
java -jar target/est-time-converter-1.0.0.jar
```

## About the offset

The default offset is 4 hours, matching "EST is 4 hours ahead" as stated by
the platform. If the platform actually follows real US Eastern clock time
rather than fixed EST, the correct offset is:

- **4 hours** during Daylight Saving Time (roughly March–November)
- **5 hours** during Standard Time (roughly November–March)

The offset is an input field on the page, so you can adjust it per lesson
or per batch without touching the code.

## Publishing this to your own GitHub (personal use)

This project is already a git repository with an initial commit. To push it
to your own GitHub account:

```bash
# from inside the est-time-converter folder
git remote add origin https://github.com/<your-username>/<your-repo-name>.git
git branch -M main
git push -u origin main
```

Create the empty repository on GitHub first (no README/license, so it stays
empty), then run the commands above with your own username and repo name.

## Project structure

```
src/main/java/com/personal/esttimeconverter/
  EstTimeConverterApplication.java   entry point
  ConvertController.java             web routes (/, /convert, /convert-bulk)
  ConvertForm.java                   form fields for the standalone time converters
  TimeConverter.java                 core conversion logic
  BulkConverter.java                 parses and converts the bulk textarea
  BulkRow.java                       one row of bulk conversion output

  roster/
    Person.java, PersonRole.java     a learner or tutor entry
    RosterService.java               loads/saves data/roster.json, Excel import
    RosterController.java            web routes (/roster, /roster/add, /roster/import)

  session/
    SessionRow.java                  one row destined for the upload sheet
    SessionForm.java                 form fields for the "new session" form
    SessionService.java              in-memory list of rows built up before download
    SessionExcelExporter.java        writes the pending rows to an .xlsx
    SessionController.java           web routes (/session/add, /sessions/download, /sessions/clear)

src/main/resources/
  templates/index.html               sessions page
  templates/roster.html              roster page
  static/styles.css                  shared styling

src/test/java/.../TimeConverterTest.java  unit tests for the conversion logic
```
