// Package declaration
package org.example.models;

/**
 * Model class representing a group that can hold multiple entries.
 * Used to categorize and manage entries under shared metadata like name and visibility.
 */
public class EntryGroup {

    // Unique identifier for the entry group (typically auto-generated in the database)
    private Long id;

    // Name/title of the group (e.g., "Workout Logs", "Diabetes Tracker")
    private String name;

    // Optional detailed description of what the group is about
    private String description;

    // Visibility status of the group (PUBLIC or PRIVATE)
    private Visibility visibility;

    // The username of the user who created this group
    private String createdBy;

    /**
     * Enum representing the visibility status of the group.
     * PUBLIC: visible to other users (depending on app rules).
     * PRIVATE: only the creator can view and manage it.
     */
    public enum Visibility {
        PUBLIC,
        PRIVATE
    }

    // ----- Getters and Setters -----

    /**
     * Gets the ID of the group.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the ID of the group.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the name of the group.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the group.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the group's description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the group's description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the visibility level of the group.
     */
    public Visibility getVisibility() {
        return visibility;
    }

    /**
     * Sets the visibility level of the group.
     */
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    /**
     * Gets the username of the user who created the group.
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the username of the user who created the group.
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
