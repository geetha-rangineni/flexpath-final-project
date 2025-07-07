// Package declaration
package org.example.controllers;

// Importing required classes
import java.util.List;

import org.example.daos.EntryDao;               // DAO for database access related to Entry
import org.example.models.Entry;                // Entry model class
import org.springframework.beans.factory.annotation.Autowired; // For dependency injection
import org.springframework.http.HttpStatus;     // For HTTP status codes
import org.springframework.security.access.prepost.PreAuthorize; // For securing endpoints
import org.springframework.security.core.Authentication; // Holds authentication info
import org.springframework.security.core.context.SecurityContextHolder; // Access security context
import org.springframework.web.bind.annotation.DeleteMapping; // Maps HTTP DELETE requests
import org.springframework.web.bind.annotation.GetMapping;     // Maps HTTP GET requests
import org.springframework.web.bind.annotation.PathVariable;   // Binds URL template variable
import org.springframework.web.bind.annotation.PostMapping;    // Maps HTTP POST requests
import org.springframework.web.bind.annotation.PutMapping;     // Maps HTTP PUT requests
import org.springframework.web.bind.annotation.RequestBody;    // Binds method parameter to request body
import org.springframework.web.bind.annotation.RequestMapping; // Maps base URL
import org.springframework.web.bind.annotation.RequestParam;   // Binds request parameter
import org.springframework.web.bind.annotation.RestController; // Indicates RESTful controller
import org.springframework.web.server.ResponseStatusException; // To throw exceptions with status codes

// Declare this class as a REST controller with base URL /api/entries
@RestController
@RequestMapping("/api/entries")
// Ensures all endpoints require authentication
@PreAuthorize("isAuthenticated()")
public class EntryController {

    // Inject EntryDao dependency to perform DB operations
    @Autowired
    private EntryDao entryDao;

    // Endpoint to get all entries, admins get all, users get only their entries
    @GetMapping
    public List<Entry> getAll() {
        // Get authentication details from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Extract the username of the logged-in user
        String username = authentication.getName();
        // Check if the user has ADMIN authority
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));

        // Return all entries for admin, or user-specific entries for regular users
        if (isAdmin) {
            return entryDao.getAll();
        }

        return entryDao.getAllByUser(username);
    }

    // Endpoint to search entries based on a field and query
    @GetMapping("/search")
    public List<Entry> searchEntry(@RequestParam String field, @RequestParam String query) {
        // Get authentication and username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        // Check if user is admin
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
        // Delegate search to DAO
        return entryDao.search(field, query, username, isAdmin);
    }

    // Get entries created by a specific user (accessible by admins or others with proper security config)
    @GetMapping("/user/{userName}")
    public List<Entry> getByUser(@PathVariable String userName) {
        // Get entries by userName from DAO
        return entryDao.getByUserId(userName);
    }

    // Create a new entry
    @PostMapping
    public Entry create(@RequestBody Entry entry) {
        // Set the current user's name as the creator
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        entry.setCreatedBy(authentication.getName());
        // Save the entry via DAO
        return entryDao.save(entry);
    }

    // Update an existing entry by ID
    @PutMapping("/{id}")
    public Entry update(@PathVariable Long id, @RequestBody Entry entry) {
        // Set the entry ID to be updated
        entry.setId(id);

        // Check if the entry with the given ID exists
        if (entryDao.findById(id).isEmpty()) {
            // Throw 404 if not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found with id " + id);
        }

        // Update the entry via DAO
        return entryDao.update(entry);
    }

    // Delete an entry by ID
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        // Check if entry exists before deletion
        if (entryDao.findById(id).isEmpty()) {
            // Throw 404 if entry not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found with id " + id);
        }
        // Delete the entry using DAO
        entryDao.deleteById(id);
    }
}
