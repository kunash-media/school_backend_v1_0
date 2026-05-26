package com.sc.util;

import com.sc.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class StudentIdGenerator {

    private static final String STUDENT_ID_PREFIX = "STD";
    private static final String ROLL_NUMBER_PREFIX = "ROLL";
    private static final int RANDOM_DIGITS = 4;
    private static final int MAX_ATTEMPTS = 10;

    private final StudentRepository studentRepository;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public StudentIdGenerator(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Generate unique student ID
    public String generateUniqueStudentId() {
        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            String candidate = generateStudentIdCandidate();
            if (!studentRepository.existsByStudentId(candidate)) {
                return candidate;
            }
            attempts++;
        }
        throw new RuntimeException("Failed to generate unique studentId after " + MAX_ATTEMPTS + " attempts.");
    }

    // Generate unique roll number
    public String generateUniqueRollNumber(String currentClass, String section) {
        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            String candidate = generateRollNumberCandidate(currentClass, section);
            if (!studentRepository.existsByStudentRollNumber(candidate)) {
                return candidate;
            }
            attempts++;
        }
        throw new RuntimeException("Failed to generate unique roll number after " + MAX_ATTEMPTS + " attempts.");
    }

    private String generateStudentIdCandidate() {
        int number = random.nextInt(10000);
        return STUDENT_ID_PREFIX + String.format("%04d", number);
    }

    private String generateRollNumberCandidate(String currentClass, String section) {
        int number = random.nextInt(1000);
        return ROLL_NUMBER_PREFIX + currentClass.replaceAll("[^0-9]", "") +
                section.toUpperCase() + String.format("%03d", number);
    }
}