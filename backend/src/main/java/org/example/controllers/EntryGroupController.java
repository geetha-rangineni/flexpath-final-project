// Package declaration
package org.example.controllers;

// Import required classes
import java.util.List;

import org.example.daos.EntryGroupDao;               // DAO for database operations related to EntryGroup
import org.example.models.EntryGroup;               // Model class representing a group of entries
import org.springframework.beans.factory.annotation.Autowired; // For dependency injection
import org.springframework.security.access.prepost.PreAuthorize; // Annotation to restrict access based on authentication
import org.springframework.security.core.Authentication; // Holds authentication data
import org.springframework.security.core.context.SecurityContextHolder; // Provides security context
import org.springframework.web.bind.annotation.DeleteMapping; // Maps DELETE HTTP requests
import org.springframework.web.bind.annotation.GetMapping;     // Maps GET HTTP requests
import org.springframework.web.bind.annotation.PathVariable;   // Binds URL path variables
import org.springframework.web.bind.annotation.PostMapping;    // Maps POST HTTP requests
import org.springframework.web.bind.annotation.PutMapping;     // Maps PUT HTTP requests
import org.springframework.web.bind.annotation.RequestBody;    // Binds method parameters to request body
import org.springframework.web.bind.annotation.RequestMapping; // Base route mapping
import org.springframework.web.bind.annotation.RequestParam;   // Binds query parameters
import org.springframework.web.bind.annotation.RestController; // Indicates this class is a REST controller

// Declare REST controller with base route "/api/groups"
@RestController
@RequestMapping("/api/groups")
// Ensure all endpoints require the user to be authenticated
@PreAuthorize("isAuthenticated()")
public class EntryGroupController {

	// Inject EntryGroupDao to interact with the database
	@Autowired
	private EntryGroupDao groupDao;

	// GET endpoint to fetch all groups or filter by optional search string
	@GetMapping
	public List<EntryGroup> getAll(@RequestParam(required = false) String search) {
		// Get current authentication object
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		// Extract current username
		String username = authentication.getName();

		// Check if user has ADMIN role
		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ADMIN"));

		// If admin, return all groups, optionally filtered by search string
		if (isAdmin) {
			return groupDao.getAll(search);
		}

		// If not admin, return only groups created by the current user
		return groupDao.getByUserId(username, search);
	}

	/*
	// (Commented Out) Search logic per field/query was planned or used previously
	@GetMapping("/search")
	public List<Entry> searchEntry(@RequestParam String field, @RequestParam String query) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
		return groupDao.search(field, query, username, isAdmin);
	}
	*/

	// GET endpoint to get groups created by a specific user
	@GetMapping("/user/{username}")
	public List<EntryGroup> getByUser(@PathVariable String username) {
		// Return all groups by the given username (null means no search filter)
		return groupDao.getByUserId(username, null);
	}

	// POST endpoint to create a new entry group
	@PostMapping
	public EntryGroup create(@RequestBody EntryGroup group) {
		// Get current authenticated user
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		// Set createdBy field to current user's username
		group.setCreatedBy(authentication.getName());
		// Save group to DB
		return groupDao.save(group);
	}

	// PUT endpoint to update an existing entry group by ID
	@PutMapping("/{id}")
	public EntryGroup update(@PathVariable Long id, @RequestBody EntryGroup entry) {
		// Set the ID on the group object to match the path variable
		entry.setId(id);
		// Update the group in DB
		return groupDao.update(entry);
	}

	// DELETE endpoint to delete a group by ID
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		// Delete the group using DAO
		groupDao.deleteById(id);
	}
}
