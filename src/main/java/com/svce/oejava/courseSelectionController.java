package com.svce.oejava;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
public class courseSelectionController {

    @GetMapping("/api/courses")
    public List<courseSelection> getCourses() {
        List<courseSelection> courses = new ArrayList<>();
        courses.add(new courseSelection(1, "Nanochemistry", "OC22001", "Dr. S. Ananda Babu", "ACH", 60, 0));
        courses.add(new courseSelection(2, "Lean Six Sigma", "OE22002", "Mr. K. Ram Prasad", "MEC", 60, 0));
        courses.add(new courseSelection(3, "Robotics and Programming Theory and Practices", "OE22004", "Mr. A. Ranjith Raj", "MEC", 45, 0));
        courses.add(new courseSelection(4, "Fundamentals of Automobile Engineering", "OE22102", "Mr. A. K Boobalasenthilraj", "AUT", 60, 0));
        courses.add(new courseSelection(5, "Introduction to Food Process Technology", "OE22203", "Mr. S. Naga Vignesh", "BIO", 60, 0));
        courses.add(new courseSelection(6, "Waste to Energy", "OF22301", "Dr. G. Sudha", "CHE", 60, 0));
        courses.add(new courseSelection(7, "Integrated Solid Waste Management", "OE22406", "Mr. R. Mathiyazhagan", "CVE", 60, 0));
        courses.add(new courseSelection(8, "Analytical Foundations", "OE22506", "Mr. T. Rajasekaran", "CSE", 60, 0));
        courses.add(new courseSelection(9, "Biomedical Engineering", "OE22601", "Mr. D. S. Purusothaman", "EEE", 60, 0));
        courses.add(new courseSelection(10, "Electric Vehicle Technology", "OE22607", "Dr. D. Amudhavalli", "EEE", 60, 0));
        courses.add(new courseSelection(11, "Fundamentals of Wireless Communication", "OE22708", "Dr. T. J. Jeyaprabha", "ECE", 60, 0));
        courses.add(new courseSelection(12, "Robotic Systems", "OE22712", "Mr. S. P. Sivagnana Subramanian", "ECE", 60, 0));
        courses.add(new courseSelection(13, "IT Essentials for Engineers", "OE22801", "Ms. M. Sugacini & Ms. B. Umadevi", "INT", 120, 0));
        courses.add(new courseSelection(14, "Statistical Methods for Engineers", "OM22001", "Dr. V. Valliammal & Dr. M. Radhakrishnan", "APM", 120, 0));
        return courses;
    }
}