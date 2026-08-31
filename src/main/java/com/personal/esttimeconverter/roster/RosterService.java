package com.personal.esttimeconverter.roster;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * Reads and writes the roster of learners and tutors, backed by the
 * database (H2 locally, PostgreSQL in production) rather than a local file
 * — so it survives restarts even on hosting platforms with no persistent
 * disk.
 */
@Service
public class RosterService {

    private final PersonRepository repository;

    public RosterService(PersonRepository repository) {
        this.repository = repository;
    }

    public List<Person> getLearners() {
        return repository.findByRoleOrderByNameAsc(PersonRole.LEARNER);
    }

    public List<Person> getTutors() {
        return repository.findByRoleOrderByNameAsc(PersonRole.TUTOR);
    }

    public Person findByEmailAndRole(String email, PersonRole role) {
        return repository.findByEmailIgnoreCaseAndRole(email, role).orElse(null);
    }

    public void addPerson(String name, String email, PersonRole role) {
        if (name == null || name.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Name and email are both required");
        }
        upsert(name.trim(), email.trim(), role);
    }

    /**
     * Imports rows from the platform's user export, whether it's a .csv file
     * or an Excel (.xlsx/.xls) file — decided by the uploaded file's name.
     * Looks for a header column containing "name" and one containing "email"
     * (case-insensitive). If a column containing "type" or "role" is also
     * present, each row's role (learner, tutor, or both) is read from it —
     * a value like "Learner | Tutor" adds the person to both lists. If no
     * such column exists, every row falls back to the given role. Existing
     * entries with the same email and role are updated rather than duplicated.
     */
    @Transactional
    public ImportResult importFromFile(MultipartFile file, PersonRole fallbackRole) throws IOException {
        String filename = file.getOriginalFilename();
        String lower = filename == null ? "" : filename.toLowerCase(Locale.ROOT);

        if (lower.endsWith(".csv")) {
            return importFromCsv(file, fallbackRole);
        } else if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
            return importFromExcel(file, fallbackRole);
        } else {
            throw new IllegalArgumentException("Unsupported file type — upload a .csv, .xlsx, or .xls file");
        }
    }

    public record ImportResult(int learnersImported, int tutorsImported) {
    }

    private ImportResult importFromCsv(MultipartFile file, PersonRole fallbackRole) throws IOException {
        try (InputStream in = file.getInputStream();
             InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setTrim(true).build().parse(reader)) {

            List<CSVRecord> records = parser.getRecords();
            if (records.isEmpty()) {
                throw new IllegalArgumentException("The uploaded file has no rows");
            }

            CSVRecord header = records.get(0);
            int nameCol = -1;
            int emailCol = -1;
            int typeCol = -1;
            for (int i = 0; i < header.size(); i++) {
                String value = header.get(i).toLowerCase(Locale.ROOT);
                if (nameCol == -1 && value.contains("name")) {
                    nameCol = i;
                }
                if (emailCol == -1 && value.contains("email")) {
                    emailCol = i;
                }
                if (typeCol == -1 && (value.contains("type") || value.contains("role"))) {
                    typeCol = i;
                }
            }

            if (nameCol == -1 || emailCol == -1) {
                throw new IllegalArgumentException(
                        "Couldn't find both a 'name' column and an 'email' column in the header row");
            }

            int learners = 0;
            int tutors = 0;
            for (int r = 1; r < records.size(); r++) {
                CSVRecord record = records.get(r);
                if (nameCol >= record.size() || emailCol >= record.size()) {
                    continue;
                }
                String name = record.get(nameCol).trim();
                String email = record.get(emailCol).trim();
                if (name.isEmpty() || email.isEmpty()) {
                    continue;
                }
                String typeText = (typeCol != -1 && typeCol < record.size()) ? record.get(typeCol) : null;
                for (PersonRole role : rolesFor(typeText, fallbackRole)) {
                    upsert(name, email, role);
                    if (role == PersonRole.LEARNER) {
                        learners++;
                    } else {
                        tutors++;
                    }
                }
            }

            return new ImportResult(learners, tutors);
        }
    }

    private ImportResult importFromExcel(MultipartFile file, PersonRole fallbackRole) throws IOException {
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalArgumentException("The uploaded file has no rows");
            }

            Row header = sheet.getRow(sheet.getFirstRowNum());
            int nameCol = -1;
            int emailCol = -1;
            int typeCol = -1;
            for (Cell cell : header) {
                String value = cellToString(cell).toLowerCase(Locale.ROOT);
                if (nameCol == -1 && value.contains("name")) {
                    nameCol = cell.getColumnIndex();
                }
                if (emailCol == -1 && value.contains("email")) {
                    emailCol = cell.getColumnIndex();
                }
                if (typeCol == -1 && (value.contains("type") || value.contains("role"))) {
                    typeCol = cell.getColumnIndex();
                }
            }

            if (nameCol == -1 || emailCol == -1) {
                throw new IllegalArgumentException(
                        "Couldn't find both a 'name' column and an 'email' column in the header row");
            }

            int learners = 0;
            int tutors = 0;
            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                String name = cellToString(row.getCell(nameCol)).trim();
                String email = cellToString(row.getCell(emailCol)).trim();
                if (name.isEmpty() || email.isEmpty()) {
                    continue;
                }
                String typeText = typeCol != -1 ? cellToString(row.getCell(typeCol)) : null;
                for (PersonRole role : rolesFor(typeText, fallbackRole)) {
                    upsert(name, email, role);
                    if (role == PersonRole.LEARNER) {
                        learners++;
                    } else {
                        tutors++;
                    }
                }
            }

            return new ImportResult(learners, tutors);
        }
    }

    /**
     * Reads which role(s) a row belongs to from its type/role text, e.g.
     * "Learner", "Tutor", or "Learner | Tutor" all work regardless of the
     * exact separator. Falls back to the given role when the text is blank,
     * absent, or doesn't mention either role by name.
     */
    private List<PersonRole> rolesFor(String typeText, PersonRole fallbackRole) {
        List<PersonRole> roles = new java.util.ArrayList<>();
        if (typeText != null && !typeText.isBlank()) {
            String lower = typeText.toLowerCase(Locale.ROOT);
            if (lower.contains("learner") || lower.contains("student")) {
                roles.add(PersonRole.LEARNER);
            }
            if (lower.contains("tutor") || lower.contains("teacher")) {
                roles.add(PersonRole.TUTOR);
            }
        }
        if (roles.isEmpty()) {
            roles.add(fallbackRole);
        }
        return roles;
    }

    private void upsert(String name, String email, PersonRole role) {
        Person existing = repository.findByEmailIgnoreCaseAndRole(email, role).orElse(null);
        if (existing != null) {
            existing.setName(name);
            repository.save(existing);
        } else {
            repository.save(new Person(name, email, role));
        }
    }

    private String cellToString(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return cell.toString();
    }
}
