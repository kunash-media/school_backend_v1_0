package com.sc.security;

import com.sc.entity.StudentEntity;
import com.sc.entity.TeacherEntity;
import com.sc.repository.StudentRepository;
import com.sc.repository.TeacherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AdminUserDetailsService adminUserDetailsService;

    // ✅ ADD THESE DEPENDENCIES
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          AdminUserDetailsService adminUserDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.adminUserDetailsService = adminUserDetailsService;
        logger.info("AuthController initialized");
    }

    // ✅ EXISTING ADMIN LOGIN (NO CHANGES)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String mobile = request.get("mobile");
        String password = request.get("password");

        logger.info("Login attempt for mobile: {}", mobile);

        if (mobile == null || mobile.trim().isEmpty()) {
            logger.warn("Login attempt with missing mobile number");
            return ResponseEntity.badRequest().body("Mobile number is required");
        }

        try {
            logger.debug("Authenticating user: {}", mobile);
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(mobile, password)
            );

            logger.debug("Authentication successful, loading user details for: {}", mobile);
            final UserDetails userDetails = adminUserDetailsService.loadUserByUsername(mobile);

            logger.debug("Generating JWT token for: {}", mobile);
            final String jwt = jwtUtil.generateToken(userDetails);

            logger.info("Login successful for mobile: {} | Token generated", mobile);

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("mobile", mobile);
            response.put("role", "ADMIN");
            response.put("redirectUrl", "dashboard.html");
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            logger.warn("Login failed - invalid credentials for mobile: {}", mobile);
            return ResponseEntity.status(401).body(Map.of("error", "Invalid mobile or password"));

        } catch (Exception e) {
            logger.error("Unexpected error during login for mobile: {}", mobile, e);
            return ResponseEntity.status(500).body(Map.of("error", "Authentication error - please try again later"));
        }
    }

    // ✅ NEW: STUDENT LOGIN ENDPOINT
    @PostMapping("/student/login")
    public ResponseEntity<?> studentLogin(@RequestBody Map<String, String> request) {
        String studentId = request.get("studentId");
        String rollNumber = request.get("rollNumber");
        String password = request.get("password");

        String identifier = studentId != null ? studentId : rollNumber;

        logger.info("Student login attempt for: {}", identifier);

        if (identifier == null || identifier.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Student ID or Roll Number is required"));
        }

        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
        }

        try {
            // Find student by ID or roll number
            StudentEntity student = studentRepository.findByStudentId(identifier);
            if (student == null) {
                student = studentRepository.findByStudentRollNumber(identifier);
            }

            if (student == null) {
                logger.warn("Student not found with identifier: {}", identifier);
                return ResponseEntity.status(401).body(Map.of("error", "Invalid student ID or password"));
            }

            // Verify password
            if (!passwordEncoder.matches(password, student.getStudentPassword())) {
                logger.warn("Invalid password for student: {}", identifier);
                return ResponseEntity.status(401).body(Map.of("error", "Invalid student ID or password"));
            }

            // Check if student is active
            if (!"Active".equals(student.getStatus())) {
                logger.warn("Inactive student attempted login: {}", identifier);
                return ResponseEntity.status(403).body(Map.of("error", "Student account is inactive. Please contact administrator."));
            }

            // Generate JWT token
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(student.getStudentId())
                    .password(student.getStudentPassword())
                    .roles("STUDENT")
                    .build();

            String token = jwtUtil.generateToken(userDetails);

            String fullName = student.getFirstName();
            if (student.getLastName() != null) {
                fullName += " " + student.getLastName();
            }

            logger.info("Student login successful: {} ({})", student.getStudentId(), fullName);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", "STUDENT");
            response.put("userId", student.getStudentId());
            response.put("rollNumber", student.getStudentRollNumber());
            response.put("name", fullName);
            response.put("redirectUrl", "student-dashboard.html");
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Unexpected error during student login", e);
            return ResponseEntity.status(500).body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    // ✅ NEW: TEACHER LOGIN ENDPOINT
    @PostMapping("/teacher/login")
    public ResponseEntity<?> teacherLogin(@RequestBody Map<String, String> request) {
        String employeeId = request.get("employeeId");
        String email = request.get("email");
        String password = request.get("password");

        String identifier = employeeId != null ? employeeId : email;

        logger.info("Teacher login attempt for: {}", identifier);

        if (identifier == null || identifier.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Employee ID or Email is required"));
        }

        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
        }

        try {
            // Find teacher by employee ID or email
            TeacherEntity teacher = teacherRepository.findByEmployeeId(identifier).orElse(null);
            if (teacher == null) {
                teacher = teacherRepository.findByEmail(identifier).orElse(null);
            }

            if (teacher == null) {
                logger.warn("Teacher not found with identifier: {}", identifier);
                return ResponseEntity.status(401).body(Map.of("error", "Invalid employee ID or password"));
            }

            // Verify password
            if (!passwordEncoder.matches(password, teacher.getTeacherPassword())) {
                logger.warn("Invalid password for teacher: {}", identifier);
                return ResponseEntity.status(401).body(Map.of("error", "Invalid employee ID or password"));
            }

            // Check if teacher is active
            if (!Boolean.TRUE.equals(teacher.getActive()) || teacher.isDeleted()) {
                logger.warn("Inactive teacher attempted login: {}", identifier);
                return ResponseEntity.status(403).body(Map.of("error", "Teacher account is inactive. Please contact administrator."));
            }

            // Generate JWT token
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(teacher.getEmployeeId())
                    .password(teacher.getTeacherPassword())
                    .roles("TEACHER")
                    .build();

            String token = jwtUtil.generateToken(userDetails);

            String fullName = teacher.getFirstName();
            if (teacher.getLastName() != null) {
                fullName += " " + teacher.getLastName();
            }

            logger.info("Teacher login successful: {} ({}) - Designation: {}",
                    teacher.getEmployeeId(), fullName, teacher.getDesignation());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", "TEACHER");
            response.put("userId", teacher.getEmployeeId());
            response.put("employeeId", teacher.getEmployeeId());
            response.put("name", fullName);
            response.put("email", teacher.getEmail());
            response.put("designation", teacher.getDesignation());
            response.put("department", teacher.getDepartment());
            response.put("redirectUrl", "teacher-dashboard.html");
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Unexpected error during teacher login", e);
            return ResponseEntity.status(500).body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    // Optional helper to get client IP
    private String getClientIp(Map<String, String> request) {
        return "unknown";
    }
}