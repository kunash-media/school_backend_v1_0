package com.sc.dto.request;


public class SubjectDetailDTO {
    private Integer subId;
    private String subjectName;
    private Integer totalMarks;

    // constructors, getters, setters
    public SubjectDetailDTO() {}


    public SubjectDetailDTO(Integer subId, String subjectName, Integer totalMarks) {
        this.subId = subId;
        this.subjectName = subjectName;
        this.totalMarks = totalMarks;
    }

    public Integer getSubId() {
        return subId;
    }

    public void setSubId(Integer subId) {
        this.subId = subId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }
}