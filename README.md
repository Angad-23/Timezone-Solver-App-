# EST time converter

A tiny personal Spring Boot app that converts a lesson time entered in EST
into the exact `yyyy-MM-dd HH:mm:ss` string a tutoring platform expects on
upload (platform time = EST + a fixed offset, default 4 hours).

Two tools on one page:
- **Single lesson** — pick a date, start time, and duration, get back
  `lesson_starttime` / `lesson_endtime`.
- **Bulk conversion** — paste multiple lines (`yyyy-MM-dd HH:mm,duration`),
  get back a table of converted rows you can copy into your upload sheet.

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
  ConvertForm.java                   form fields for the single-lesson form
  TimeConverter.java                 core conversion logic
  BulkConverter.java                 parses and converts the bulk textarea
  BulkRow.java                       one row of bulk conversion output
src/main/resources/templates/index.html   the page (form + results)
src/test/java/.../TimeConverterTest.java  unit tests for the conversion logic
```
