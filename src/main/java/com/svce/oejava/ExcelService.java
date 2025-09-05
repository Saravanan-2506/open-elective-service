package com.svce.oejava;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ExcelService {

    @Value("${app.data.dir:./data}")
    private String dataDir;

    @Value("${app.courses.filename:Courses.xlsx}")
    private String coursesFileName;

    @Value("${app.enrollments.filename:Enrollments.xlsx}")
    private String enrollmentsFileName;

    private final ClassPathResource coursesResource = new ClassPathResource("Courses.xlsx");

    private Path runtimeCoursesPath;
    private Path enrollmentsPath;

    @PostConstruct
    public void init() {
        try {
            Path dataDirPath = Path.of(dataDir);
            if (!Files.exists(dataDirPath)) {
                Files.createDirectories(dataDirPath);
            }
            runtimeCoursesPath = dataDirPath.resolve(coursesFileName);
            enrollmentsPath   = dataDirPath.resolve(enrollmentsFileName);

            if (!Files.exists(runtimeCoursesPath)) {
                try (InputStream in = coursesResource.getInputStream()) {
                    Files.copy(in, runtimeCoursesPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            // Enrollments.xlsx created lazily when first write happens
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Excel storage", e);
        }
    }

    /**
     * Read all courses (columns expected):
     * 0:ID, 1:Title, 2:Code, 3:Instructor, 4:Department, 5:Capacity, 6:Enrolled, 7:Restricted
     */
    public List<courseSelection> getCourses() {
        List<courseSelection> courses = new ArrayList<>();
        try (InputStream fis = Files.newInputStream(runtimeCoursesPath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) rowIterator.next(); // skip header

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                int id          = getInt(row, 0);
                String title    = getString(row, 1);
                String code     = getString(row, 2);
                String instr    = getString(row, 3);
                String dept     = getString(row, 4);
                int capacity    = getInt(row, 5);
                int enrolled    = getInt(row, 6);
                String restricted = getString(row, 7);

                if (id <= 0 || title == null || title.isBlank()) continue;

                courses.add(new courseSelection(
                        id, title, code, instr, dept, capacity, enrolled, restricted
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * Save enrollment and increment course's enrolled count in runtime Courses.xlsx
     * Now: prevents student from enrolling in multiple courses (only one allowed).
     *
     * Synchronized to avoid concurrent file writes from multiple threads.
     */
    public synchronized String saveEnrollment(String rollNo, String name, String dept, courseSelection selectedCourse) {
        try {
            // --- 1) Append to Enrollments.xlsx (create with header if missing) ---
            Workbook enrollWb;
            Sheet enrollSheet;

            if (Files.exists(enrollmentsPath)) {
                try (InputStream in = Files.newInputStream(enrollmentsPath)) {
                    enrollWb = new XSSFWorkbook(in);
                }
                enrollSheet = enrollWb.getSheetAt(0);

                // ✅ Check for any previous enrollment by this roll number
                for (Row r : enrollSheet) {
                    if (r.getRowNum() == 0) continue; // skip header
                    String existingRoll = getString(r, 0);
                    String existingCourseCode = getString(r, 3);
                    String existingCourseTitle = getString(r, 4);

                    if (existingRoll.equalsIgnoreCase(rollNo)) {
                        // If same course, block duplicate
                        if (existingCourseCode.equalsIgnoreCase(selectedCourse.getCode())) {
                            enrollWb.close();
                            return "⚠️ Student with Roll No " + rollNo +
                                   " has already enrolled in " + selectedCourse.getTitle() + "!";
                        } else {
                            // If different course, block multiple course selection
                            enrollWb.close();
                            return "❌ Student with Roll No " + rollNo +
                                   " has already enrolled in another course (" + existingCourseTitle + ").";
                        }
                    }
                }

            } else {
                enrollWb = new XSSFWorkbook();
                enrollSheet = enrollWb.createSheet("Enrollments");

                Row header = enrollSheet.createRow(0);
                header.createCell(0).setCellValue("RollNo");
                header.createCell(1).setCellValue("Name");
                header.createCell(2).setCellValue("Department");
                header.createCell(3).setCellValue("Course Code");
                header.createCell(4).setCellValue("Course Title");
            }

            // ✅ If no enrollment exists yet, allow adding
            int last = enrollSheet.getLastRowNum();
            Row row = enrollSheet.createRow(last + 1);
            row.createCell(0).setCellValue(rollNo);
            row.createCell(1).setCellValue(name);
            row.createCell(2).setCellValue(dept);
            row.createCell(3).setCellValue(selectedCourse.getCode());
            row.createCell(4).setCellValue(selectedCourse.getTitle());

            try (OutputStream out = Files.newOutputStream(enrollmentsPath)) {
                enrollWb.write(out);
            }
            enrollWb.close();

            // --- 2) Increment "Enrolled" for the chosen course in runtime Courses.xlsx ---
            Workbook coursesWb;
            try (InputStream in = Files.newInputStream(runtimeCoursesPath)) {
                coursesWb = new XSSFWorkbook(in);
            }
            Sheet courseSheet = coursesWb.getSheetAt(0);

            for (Row r : courseSheet) {
                if (r.getRowNum() == 0) continue; // header
                int courseId = getInt(r, 0);
                if (courseId == selectedCourse.getId()) {
                    int enrolled = getInt(r, 6);
                    Cell enrolledCell = r.getCell(6, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    enrolledCell.setCellValue(enrolled + 1);
                    break;
                }
            }

            try (OutputStream out = Files.newOutputStream(runtimeCoursesPath)) {
                coursesWb.write(out);
            }
            coursesWb.close();

            return "✅ Enrollment successful for " + name + " in " + selectedCourse.getTitle();

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error while enrolling student!";
        }
    }

    /**
     * Return all courses this roll number has enrolled in (0..N courses)
     */
    public List<courseSelection> getEnrolledCoursesByRoll(String rollNo) {
        List<courseSelection> enrolled = new ArrayList<>();
        try {
            if (!Files.exists(enrollmentsPath)) return enrolled;

            List<courseSelection> courses = getCourses();
            try (InputStream in = Files.newInputStream(enrollmentsPath);
                 Workbook wb = new XSSFWorkbook(in)) {
                Sheet sheet = wb.getSheetAt(0);
                for (Row r : sheet) {
                    if (r.getRowNum() == 0) continue; // skip header
                    String existingRoll = getString(r, 0);
                    String courseCode = getString(r, 3);
                    if (rollNo.equalsIgnoreCase(existingRoll)) {
                        for (courseSelection c : courses) {
                            if (c.getCode() != null && c.getCode().equalsIgnoreCase(courseCode)) {
                                enrolled.add(c);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return enrolled;
    }

    // ----------------- helpers -----------------

    private static String getString(Row row, int idx) {
        Cell c = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (c == null) return "";
        return switch (c.getCellType()) {
            case STRING -> c.getStringCellValue().trim();
            case NUMERIC -> {
                double v = c.getNumericCellValue();
                yield String.valueOf((long) v);
            }
            case BOOLEAN -> String.valueOf(c.getBooleanCellValue());
            default -> "";
        };
    }

    private static int getInt(Row row, int idx) {
        Cell c = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (c == null) return 0;
        return switch (c.getCellType()) {
            case NUMERIC -> (int) Math.round(c.getNumericCellValue());
            case STRING -> {
                try { yield Integer.parseInt(c.getStringCellValue().trim()); }
                catch (NumberFormatException e) { yield 0; }
            }
            default -> 0;
        };
    }
}
