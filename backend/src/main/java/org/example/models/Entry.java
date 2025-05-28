package org.example.models;

import java.util.Date;

public class Entry {
    private Long id;
    private String title;
    private EntryType type;
    private String description;
    private EntryGroup.Visibility visibility;
    private Date date;
    private String createdBy;
    private EntryGroup group;

    public enum EntryType {
        Workout, Diet, Symptom, Other
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public EntryType getType() {
        return type;
    }

    public void setType(EntryType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EntryGroup.Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(EntryGroup.Visibility visibility) {
        this.visibility = visibility;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public EntryGroup getGroup() {
        return group;
    }

    public void setGroup(EntryGroup group) {
        this.group = group;
    }
}
