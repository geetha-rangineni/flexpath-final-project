// Package declaration
package org.example.controllers;

// Import required classes
import org.example.daos.UserDao;                            // DAO interface to access User data
import org.example.models.User;                             // User model class
import org.springframework.beans.factory.annotation.Autowired; // Enables dependency injection
import org.springframework.security.access.prepost.PreAuthorize; // For securing endpoints
import org.springframework.web.bind.annotation.*;

import java.security.Principal;                            // Represents the currently logged-in user
import java.util.List;

/**
 * REST Controller for accessing and updating the currently logged-in user's profile.
 */
@RestController
@RequestMapping("/api/profile")                             // Base URL for all profile-related endpoints
@PreAuthorize("isAuthenticated()")                         // Restricts access to authenticated users only
public class ProfileController {

    /**
     * Injects UserDao for accessing user information from the database.
     */
    @Autowired
    private UserDao userDao;

    /**
     * GET endpoint to fetch the profile of the currently authenticated user.
     *
     * @param principal The Principal object representing the logged-in user.
     * @return The User object for the currently authenticated user.
     */
    @GetMapping
    public User getProfile(Principal principal) {
        // Extract the username from the Principal
        String username = principal.getName();
        // Fetch and return the User object from the DAO
        return userDao.getUserByUsername(username);
    }

    /**
     * GET endpoint to fetch all roles of the currently authenticated user.
     *
     * @param principal The Principal object representing the logged-in user.
     * @return A list of roles assigned to the user.
     */
    @GetMapping("/roles")
    public List<String> getRoles(Principal principal) {
        // Extract the username
        String username = principal.getName();
        // Retrieve user roles from DAO
        return userDao.getRoles(username);
    }

    /**
     * PUT endpoint to change the password of the currently logged-in user.
     *
     * @param principal   The Principal object representing the logged-in user.
     * @param newPassword The new password string passed in the request body.
     * @return The updated User object after the password change.
     */
    @PutMapping("/change-password")
    public User changePassword(Principal principal, @RequestBody String newPassword) {
        // Get the current user's username
        String username = principal.getName();
        // Fetch the user object
        User user = userDao.getUserByUsername(username);
        // Set the new password
        user.setPassword(newPassword);
        // Persist the updated user and return
        return userDao.updatePassword(user);
    }
}
