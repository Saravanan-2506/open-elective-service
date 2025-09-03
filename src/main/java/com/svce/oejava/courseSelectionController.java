package com.svce.oejava;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
public class courseSelectionController {

    @Autowired
    private ExcelService excelService;

    // Return courses with restriction & capacity checks
    @GetMapping("/api/courses/{email}")
    public List<courseSelection> getCourses(@PathVariable String email) {
        String dept = extractDeptFromEmail(email); // IT, CS, ME, ...

        List<courseSelection> courses = excelService.getCourses();
        if (courses == null || courses.isEmpty()) {
            courses = getHardcodedCourses(); // fallback
        }

        List<courseSelection> finalList = new ArrayList<>();
        for (courseSelection c : courses) {
            if (c == null || c.getId() <= 0 || c.getTitle() == null || c.getTitle().isBlank()) {
                continue;
            }

            // full if enrolled >= capacity
            if (c.getEnrolled() >= c.getCapacity()) {
                c.setFull(true);
                c.setDisabled(true);
            }

            // disable if this student's department is listed in restricted
            if (c.getRestricted() != null && !c.getRestricted().isBlank()) {
                String[] restrictedDepts = c.getRestricted().toUpperCase().split("[, /]+");
                for (String r : restrictedDepts) {
                    if (dept.equals(r.trim())) {
                        c.setDisabled(true);
                        break;
                    }
                }
            }
            finalList.add(c);
        }

        return finalList;
    }

    // Fallback if frontend calls without email
    @GetMapping("/api/courses")
    public List<courseSelection> getCoursesDefault() {
        return getCourses("test@svce.ac.in");
    }

    // Save enrollment and update counts
    @PostMapping("/api/enroll")
    public String enrollStudent(@RequestBody EnrollmentRequest request) {
        List<courseSelection> courses = excelService.getCourses();

        courseSelection selectedCourse = courses.stream()
                .filter(c -> c.getId() == request.getCourseId())
                .findFirst()
                .orElse(null);

        if (selectedCourse == null) return "Course not found!";

        if (selectedCourse.getEnrolled() >= selectedCourse.getCapacity()) {
            return "Course is already full!";
        }

        return excelService.saveEnrollment(
                request.getRollNo(),
                request.getName(),
                request.getDepartment(),
                selectedCourse
        );
    }

    /**
     * Return all courses this student (email -> roll) has enrolled in.
     * frontend expects an array (0..N)
     */
    @GetMapping("/api/enrolled/{email}")
    public List<courseSelection> getEnrolledCourses(@PathVariable String email) {
        try {
            String roll = email.split("@")[0];
            return excelService.getEnrolledCoursesByRoll(roll);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Helpers
    private String extractDeptFromEmail(String email) {
        try {
            String roll = email.split("@")[0];               // e.g., 2024it0007
            String dept = roll.replaceAll("[0-9]", "");      // it
            return dept.toUpperCase();                       // IT
        } catch (Exception e) { return ""; }
    }

    // Hardcoded fallback (unchanged)
    private List<courseSelection> getHardcodedCourses() {
        List<courseSelection> courses = new ArrayList<>();
        courses.add(new courseSelection(1, "Nanochemistry", "OC22001", "Dr. S. Ananda Babu", "ACH", 60, 0, "IT,INT"));
        courses.add(new courseSelection(2, "Lean Six Sigma", "OE22002", "Mr. K. Ram Prasad", "MEC", 60, 0, "CS"));
        courses.add(new courseSelection(3, "Robotics and Programming Theory and Practices", "OE22004", "Mr. A. Ranjith Raj", "MEC", 45, 0, ""));
        courses.add(new courseSelection(4, "Fundamentals of Automobile Engineering", "OE22102", "Mr. A. K Boobalasenthilraj", "AUT", 60, 0, ""));
        courses.add(new courseSelection(5, "Introduction to Food Process Technology", "OE22203", "Mr. S. Naga Vignesh", "BIO", 60, 0, ""));
        courses.add(new courseSelection(6, "Waste to Energy", "OF22301", "Dr. G. Sudha", "CHE", 60, 0, ""));
        courses.add(new courseSelection(7, "Integrated Solid Waste Management", "OE22406", "Mr. R. Mathiyazhagan", "CVE", 60, 0, ""));
        courses.add(new courseSelection(8, "Analytical Foundations", "OE22506", "Mr. T. Rajasekaran", "CSE", 60, 0, ""));
        courses.add(new courseSelection(9, "Biomedical Engineering", "OE22601", "Mr. D. S. Purusothaman", "EEE", 60, 0, ""));
        courses.add(new courseSelection(10, "Electric Vehicle Technology", "OE22607", "Dr. D. Amudhavalli", "EEE", 60, 0, ""));
        courses.add(new courseSelection(11, "Fundamentals of Wireless Communication", "OE22708", "Dr. T. J. Jeyaprabha", "ECE", 60, 0, ""));
        courses.add(new courseSelection(12, "Robotic Systems", "OE22712", "Mr. S. P. Sivagnana Subramanian", "ECE", 60, 0, ""));
        courses.add(new courseSelection(13, "IT Essentials for Engineers", "OE22801", "Ms. M. Sugacini & Ms. B. Umadevi", "INT", 120, 0, "IT,INT"));
        courses.add(new courseSelection(14, "Statistical Methods for Engineers", "OM22001", "Dr. V. Valliammal & Dr. M. Radhakrishnan", "APM", 120, 0, ""));
        return courses;
    }
}
