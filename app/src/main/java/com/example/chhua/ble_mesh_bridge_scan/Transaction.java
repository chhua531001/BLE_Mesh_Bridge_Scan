package com.example.chhua.ble_mesh_bridge_scan;

/**
 * Created by chhua on 2017/8/4.
 */

public class Transaction {
    String priority;
    String type;
    String subject;
    String detail;
    Long timestamp;

    public Transaction() {
    }

    public Transaction(String priority, String type, String subject, String detail, Long timestamp) {
        this.priority = priority;
        this.type = type;
        this.subject = subject;
        this.detail = detail;
        this.timestamp = timestamp;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}


