package com.chai.tackle.Model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;

public class Ticket {
    private String creatorId;
    private String category;
    private String subject;
    private String description;
    private int status;
    private ArrayList<String> attachments;
    private Date timestamp;

    public Ticket() {
        // Needed for Firebase
    }

    public Ticket(String creatorId, String category, String subject, String description, int status, ArrayList<String> attachments) {
        this.creatorId = creatorId;
        this.category = category;
        this.subject = subject;
        this.description = description;
        this.status = status;
        this.attachments = attachments;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ArrayList<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<String> attachments) {
        this.attachments = attachments;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
