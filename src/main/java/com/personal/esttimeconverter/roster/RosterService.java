package com.personal.esttimeconverter.roster;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Keeps the list of learners and tutors on disk as a small JSON file, so it
 * survives app restarts. Not built for concurrent multi-user access — this
 * app is meant for one person's personal use.
 */
@Service
public class RosterService {

    private final Path rosterFile;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<Person> people = new CopyOnWriteArrayList<>();

    public RosterService(@Value("${app.roster-file:data/roster.json}") String rosterFilePath) throws IOException {
        this.rosterFile = Path.of(rosterFilePath);
        load();
    }

    private synchronized void load() throws IOException {
        people.clear();
        if (Files.exists(rosterFile)) {
            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, Person.class);
            List<Person> loaded = objectMapper.readValue(rosterFile.toFile(), listType);
            people.addAll(loaded);
        }
    }

    private synchronized void save() throws IOException {
        File parent = rosterFile.toFile().getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(rosterFile.toFile(), people);
    }

    public List<Person> getLearners() {
        return byRole(PersonRole.LEARNER);
    }

    public List<Person> getTutors() {
        return byRole(PersonRole.TUTOR);
    }

    private List<Person> byRole(PersonRole role) {
        List<Person> result = new ArrayList<>();
        for (Person p : people) {
            if (p.getRole() == role) {
                result.add(p);
            }
        }
        result.sort(Comparator.comparing(p -> p.getName().toLowerCase(Locale.ROOT)));
        return result;
    }

    public Person findByEmailAndRole(String email, PersonRole role) {
        for (Person p : people) {
            if (p.getRole() == role && p.getEmail().equalsIgnoreCase(email)) {
                return p;
            }
        }
        return null;
    }

    public synchronized void addPerson(String name, String email, PersonRole role) throws IOException {
        if (name == null || name.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Name and email are both required");
        }
        upsert(name.trim(), email.trim(), role);
        save();
    }

    /**
     * Imports rows from the platform's user export, whether it's a .csv file
     * or an Excel (.xlsx/.xls) file — decided by the uploaded file's name.
     * Looks for a header row/line containing a column with "name" in it and a
     * column with "email" in it (case-insensitive); every person found is
     * added under the given role. Existing entries with the same email and
     * role are updated rather than duplicated.
     *
     * @return number of people imported
     */
    public synchronized int importFromFile(MultipartFile file, PersonRole role) throws IOException {
        String filename = file.getOriginalFilename();
        String lower = filename == null ? "" : filename.toLowerCase(Locale.ROOT);

        int imported;
        if (lower.endsWith(".csv")) {
            imported = importFromCsv(file, role);
        } else if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
            imported = importFromExcel(file, role);
        } else {
            throw new IllegalArgumentException("Unsupported file type — upload a .csv, .xlsx, or .xls file");
        }

        save();
        return imported;
    }

    private int importFromCsv(MultipartFile file, PersonRole role) throws IOException {
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
            for (int i = 0; i < header.size(); i++) {
                String value = header.get(i).toLowerCase(Locale.ROOT);
                if (nameCol == -1 && value.contains("name")) {
                    nameCol = i;
                }
                if (emailCol == -1 && value.contains("email")) {
                    emailCol = i;
                }
            }

            if (nameCol == -1 || emailCol == -1) {
                throw new IllegalArgumentException(
                        "Couldn't find both a 'name' column and an 'email' column in the header row");
            }

            int imported = 0;
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
                upsert(name, email, role);
                imported++;
            }

            return imported;
        }
    }

    private int importFromExcel(MultipartFile file, PersonRole role) throws IOException {
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalArgumentException("The uploaded file has no rows");
            }

            Row header = sheet.getRow(sheet.getFirstRowNum());
            int nameCol = -1;
            int emailCol = -1;
            for (Cell cell : header) {
                String value = cellToString(cell).toLowerCase(Locale.ROOT);
                if (nameCol == -1 && value.contains("name")) {
                    nameCol = cell.getColumnIndex();
                }
                if (emailCol == -1 && value.contains("email")) {
                    emailCol = cell.getColumnIndex();
                }
            }

            if (nameCol == -1 || emailCol == -1) {
                throw new IllegalArgumentException(
                        "Couldn't find both a 'name' column and an 'email' column in the header row");
            }

            int imported = 0;
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
                upsert(name, email, role);
                imported++;
            }

            return imported;
        }
    }

    private void upsert(String name, String email, PersonRole role) {
        for (Person p : people) {
            if (p.getRole() == role && p.getEmail().equalsIgnoreCase(email)) {
                p.setName(name);
                return;
            }
        }
        people.add(new Person(name, email, role));
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

