package com.sc.dto.request;

import java.util.List;

public class BulkActionDto {
    private List<Long> assignmentIds;
    private String actionType;
    private String newStatus;
    private String reminderType;
    private String customMessage;

    // Getters and Setters
    public List<Long> getAssignmentIds() { return assignmentIds; }
    public void setAssignmentIds(List<Long> assignmentIds) { this.assignmentIds = assignmentIds; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getReminderType() { return reminderType; }
    public void setReminderType(String reminderType) { this.reminderType = reminderType; }

    public String getCustomMessage() { return customMessage; }
    public void setCustomMessage(String customMessage) { this.customMessage = customMessage; }
}