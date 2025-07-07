// Package declaration
package org.example.models;

import java.util.Date; // Importing Java Date class for representing date/time

/**
 * Model class representing a user's activity or health-related entry.
 * Each entry belongs to a group and contains metadata like type, description, and visibility.
 */
public class Entry {

    // Unique identifier for the entry (auto-generated)
    private Long id;

    // Title or short label for the entry (e.g., "Morning Run", "Headache", etc.)
    private String title;

    // Type of entry (Workout, Diet, Symptom, Other)
    private EntryType type;

    // A detailed description of the entry
    private String description;

    // Visibility level for the entry (PRIVATE, SHARED, PUBLIC) - inherited from EntryGroup.Visibility
    private EntryGroup.Visibility visibility;

    // The date when the entry was logged or occurred
    private Date date;

    // The username of the person who created this entry
    private String createdBy;

    // The group this entry is associated with (e.g., "Fitness", "Nutrition")
    private EntryGroup group;

    /**
     * Enum representing the category/type of the entry.
     */
    public enum EntryType {
        Workout, // Physical activity
        Diet,    // Food/nutrition log
        Symptom, // Health-related symptom
        Other    // Any other entry not categorized above
    }

    // ----- Getters and Setters -----

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
