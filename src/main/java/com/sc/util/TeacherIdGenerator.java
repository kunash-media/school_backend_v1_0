package com.sc.util;

import com.sc.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.text.DecimalFormat;

@Component  // Make it a Spring bean
public class TeacherIdGenerator {

    private static final String PREFIX = "TCH";
    private static final DecimalFormat ID_FORMAT = new DecimalFormat("000");

    private static TeacherRepository teacherRepository;
    private static int lastGeneratedNumber = 0;

    @Autowired
    public void setTeacherRepository(TeacherRepository repo) {
        teacherRepository = repo;
        initializeCounter();
    }

    /**
     * Initialize counter from database
     */
    private void initializeCounter() {
        try {
            // Query database for max teacher code sequence
            Integer maxSequence = teacherRepository.findMaxTeacherCodeSequence();
            if (maxSequence != null && maxSequence > 0) {
                lastGeneratedNumber = maxSequence;
                System.out.println("DEBUG - Initialized teacher ID counter from DB: " + lastGeneratedNumber);
            } else {
                lastGeneratedNumber = 0;
                System.out.println("DEBUG - No existing teachers, starting from 0");
            }
        } catch (Exception e) {
            e.printStackTrace();
            lastGeneratedNumber = 0; // Default to 0 if error
        }
    }

    /**
     * Generates a new teacher ID in the format TCH001, TCH002, etc.
     * Now thread-safe and database-aware
     */
    public static synchronized String generateTeacherId() {
        lastGeneratedNumber++;
        String newId = PREFIX + ID_FORMAT.format(lastGeneratedNumber);
        System.out.println("DEBUG - Generated new teacher ID: " + newId);
        return newId;
    }

    /**
     * Generates teacher ID based on sequence number
     */
    public static String generateTeacherId(int sequenceNumber) {
        return PREFIX + ID_FORMAT.format(sequenceNumber);
    }

    /**
     * Validates if a teacher ID is in the correct format
     */
    public static boolean isValidTeacherId(String teacherId) {
        if (teacherId == null || teacherId.length() < 4) {
            return false;
        }

        if (!teacherId.startsWith(PREFIX)) {
            return false;
        }

        String numericPart = teacherId.substring(PREFIX.length());
        try {
            Integer.parseInt(numericPart);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Extracts the sequence number from a teacher ID
     */
    public static int extractSequenceNumber(String teacherId) {
        if (!isValidTeacherId(teacherId)) {
            return 0;
        }

        String numericPart = teacherId.substring(PREFIX.length());
        try {
            return Integer.parseInt(numericPart);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Gets the next teacher ID based on an existing one
     */
    public static String getNextTeacherId(String currentTeacherId) {
        if (!isValidTeacherId(currentTeacherId)) {
            return PREFIX + "001";
        }

        int currentSeq = extractSequenceNumber(currentTeacherId);
        return generateTeacherId(currentSeq + 1);
    }

    /**
     * Resets the counter (useful for testing)
     */
    public static void resetCounter() {
        lastGeneratedNumber = 0;
    }

    /**
     * Gets the current counter value
     */
    public static int getLastGeneratedNumber() {
        return lastGeneratedNumber;
    }

    /**
     * Force refresh counter from database
     */
    public static void refreshCounter() {
        if (teacherRepository != null) {
            Integer maxSequence = teacherRepository.findMaxTeacherCodeSequence();
            if (maxSequence != null && maxSequence > 0) {
                lastGeneratedNumber = maxSequence;
            }
        }
    }
}