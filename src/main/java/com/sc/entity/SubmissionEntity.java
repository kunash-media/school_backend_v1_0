package com.sc.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submissions")
public class SubmissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long submissionId;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private AssignmentEntity assignment;

    @ManyToOne
    @JoinColumn(name = "student_std_id", nullable = false)
    private StudentEntity student;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "roll_number")
    private String rollNumber;

    @Column(name = "student_class")
    private String studentClass;

    @Column(name = "student_section")
    private String studentSection;

    @Column(name = "submitted_date")
    private LocalDateTime submittedDate;

    @ElementCollection
    @CollectionTable(name = "submission_files",
            joinColumns = @JoinColumn(name = "submission_id"))
    @Column(name = "file_name")
    private List<String> files = new ArrayList<>();

    @Column(nullable = false)
    private String status;

    @Column(name = "obtained_marks")
    private Float obtainedMarks;

    private String grade;

    @Column(name = "teacher_feedback", columnDefinition = "TEXT")
    private String teacherFeedback;

    @Column(name = "is_late")
    private Boolean isLate = false;

    @Column(name = "is_resubmission")
    private Boolean isResubmission = false;

    @Column(name = "recheck_requested")
    private Boolean recheckRequested = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ============= CONSTRUCTORS =============

    public SubmissionEntity() {
    }

    // ============= HELPER METHOD TO SET STUDENT =============
    public void setStudent(StudentEntity student) {
        this.student = student;
        if (student != null) {
            this.studentName = student.getFirstName() + " " +
                    (student.getLastName() != null ? student.getLastName() : "");
            this.rollNumber = student.getStudentRollNumber();
            this.studentClass = student.getCurrentClass();
            this.studentSection = student.getSection();
        }
    }

    // ============= GETTERS & SETTERS =============

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public AssignmentEntity getAssignment() {
        return assignment;
    }

    public void setAssignment(AssignmentEntity assignment) {
        this.assignment = assignment;
    }

    public StudentEntity getStudent() {
        return student;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getStudentClass() {
        return studentClass;
    }

    public void setStudentClass(String studentClass) {
        this.studentClass = studentClass;
    }

    public String getStudentSection() {
        return studentSection;
    }

    public void setStudentSection(String studentSection) {
        this.studentSection = studentSection;
    }

    public LocalDateTime getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(LocalDateTime submittedDate) {
        this.submittedDate = submittedDate;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Float getObtainedMarks() {
        return obtainedMarks;
    }

    public void setObtainedMarks(Float obtainedMarks) {
        this.obtainedMarks = obtainedMarks;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getTeacherFeedback() {
        return teacherFeedback;
    }

    public void setTeacherFeedback(String teacherFeedback) {
        this.teacherFeedback = teacherFeedback;
    }

    public Boolean getIsLate() {
        return isLate;
    }

    public void setIsLate(Boolean isLate) {
        this.isLate = isLate;
    }

    public Boolean getIsResubmission() {
        return isResubmission;
    }

    public void setIsResubmission(Boolean isResubmission) {
        this.isResubmission = isResubmission;
    }

    public Boolean getRecheckRequested() {
        return recheckRequested;
    }

    public void setRecheckRequested(Boolean recheckRequested) {
        this.recheckRequested = recheckRequested;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}