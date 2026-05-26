package com.sc.util;

import com.sc.repository.AssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Random;

@Component
public class AssignmentIdGenerator {

    @Autowired
    private AssignmentRepository assignmentRepository;

    public String generateUniqueAssignmentCode() {
        String prefix = "ASSIGN";
        String year = String.valueOf(Year.now().getValue());
        Random random = new Random();
        String code;
        boolean isUnique;

        do {
            int randomNum = 100 + random.nextInt(900); // 3-digit random number
            code = prefix + "-" + year + "-" + randomNum;
            isUnique = assignmentRepository.findByAssignmentCode(code) == null;
        } while (!isUnique);

        return code;
    }
}