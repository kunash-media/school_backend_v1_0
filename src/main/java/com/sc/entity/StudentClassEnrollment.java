package com.sc.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "student_class_enrollment")  // your child collection table
public class StudentClassEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // or use composite key if you prefer no surrogate PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_std_id", nullable = false)
    private StudentEntity student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    // Duplicated / snapshot fields (as requested)
    @Column(name = "class_name", nullable = false)
    private String className;

    @Column(name = "section", nullable = false)
    private String section;

    @Column(name = "student_name", nullable = false)
    private String studentName;  // e.g. first + middle + last or just first+last

    @Column(name = "student_roll_number", nullable = false)
    private String studentRollNumber;

    // Optional: when enrolled (useful if historical)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "enrolled_at")
    private Date enrolledAt = new Date();

    // Optional: status if student can be inactive in class etc.
    private String status = "ACTIVE";

    // Constructors
    public StudentClassEnrollment() {}

    // Recommended constructor to copy values
    public StudentClassEnrollment(StudentEntity student, ClassEntity clazz) {
        this.student = student;
        this.classEntity = clazz;
        this.className = clazz.getClassName();
        this.section = clazz.getSection();
        this.studentName = (student.getFirstName() + " " +
                (student.getMiddleName() != null ? student.getMiddleName() + " " : "") +
                student.getLastName()).trim();
        this.studentRollNumber = student.getStudentRollNumber();
        this.enrolledAt = new Date();
    }

    // Getters & Setters (add them all)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StudentEntity getStudent() { return student; }
    public void setStudent(StudentEntity student) { this.student = student; }
    public ClassEntity getClassEntity() { return classEntity; }
    public void setClassEntity(ClassEntity classEntity) { this.classEntity = classEntity; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentRollNumber() { return studentRollNumber; }
    public void setStudentRollNumber(String studentRollNumber) { this.studentRollNumber = studentRollNumber; }
    public Date getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(Date enrolledAt) { this.enrolledAt = enrolledAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}