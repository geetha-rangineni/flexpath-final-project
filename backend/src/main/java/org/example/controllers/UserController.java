// Package declaration
package org.example.controllers;

// Importing required classes
import org.example.models.User;                              // User model class
import org.example.daos.UserDao;                             // DAO for user-related DB operations
import org.springframework.beans.factory.annotation.Autowired; // For dependency injection
import org.springframework.http.HttpStatus;                  // HTTP status codes
import org.springframework.security.access.prepost.PreAuthorize; // Role-based access control
import org.springframework.web.bind.annotation.*;            // Spring Web annotations
import org.springframework.web.server.ResponseStatusException; // To throw status-based errors

import java.util.List;

/**
 * REST Controller for handling user management operations.
 * Restricted to users with ADMIN authority (except for user creation).
 */
@RestController
@CrossOrigin                                               // Allows cross-origin requests (CORS)
@RequestMapping("/api/users")                              // Base path for user-related endpoints
@PreAuthorize("hasAuthority('ADMIN')")                    // Restrict access to admin users
public class UserController {

    /**
     * Injected Data Access Object for performing DB operations on users.
     */
    @Autowired
    private UserDao userDao;

    /**
     * GET endpoint to fetch all users.
     *
     * @return List of all user accounts.
     */
    @GetMapping
    public List<User> getAll() {
        return userDao.getUsers();
    }

    /**
     * GET endpoint to fetch a user by their username.
     *
     * @param username The username to search for.
     * @return The corresponding user, if found.
     */
    @GetMapping(path = "/{username}")
    public User get(@PathVariable String username) {
        return userDao.getUserByUsername(username);
    }

    /**
     * POST endpoint to create a new user.
     * This endpoint is publicly accessible (no auth required).
     *
     * @param user The user details to be created.
     * @return The created user object.
     */
    @ResponseStatus(HttpStatus.CREATED)                   // Respond with HTTP 201
    @PostMapping
    @PreAuthorize("permitAll()")                          // Allow anyone to access this endpoint
    public User create(@RequestBody User user) {
        return userDao.createUser(user);
    }

    /**
     * PUT endpoint to update a user's password.
     *
     * @param password The new password string (sent in the request body).
     * @param username The target username.
     * @return The updated user.
     */
    @PutMapping(path = "/{username}/password")
    public User updatePassword(@RequestBody String password, @PathVariable String username) {
        // Fetch user by username
        User user = userDao.getUserByUsername(username);

        // Throw 404 if user not found
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        // Set new password and update user
        user.setPassword(password);
        return userDao.updatePassword(user);
    }

    /**
     * DELETE endpoint to delete a user by their username.
     *
     * @param username Username of the user to delete.
     * @return Number of rows affected.
     */
    @DeleteMapping(path = "/{username}")
    public int delete(@PathVariable String username) {
        return userDao.deleteUser(username);
    }

    /**
     * GET endpoint to retrieve all roles assigned to a user.
     *
     * @param username Username of the user.
     * @return List of role names.
     */
    @GetMapping(path = "/{username}/roles")
    public List<String> getRoles(@PathVariable String username) {
        return userDao.getRoles(username);
    }

    /**
     * POST endpoint to add a role to a user.
     *
     * @param username Username of the user.
     * @param role Role name to be added (passed in request body).
     * @return Updated list of roles for the user.
     */
    @PostMapping(path = "/{username}/roles")
    public List<String> addRole(@PathVariable String username, @RequestBody String role) {
        return userDao.addRole(username, role.toUpperCase());
    }

    /**
     * DELETE endpoint to remove a specific role from a user.
     *
     * @param username Username of the user.
     * @param role Role name to remove.
     * @return Number of rows affected.
     */
    @DeleteMapping(path = "/{username}/roles/{role}")
    public int deleteRole(@PathVariable String username, @PathVariable String role) {
        var affectedRows = userDao.deleteRole(username, role.toUpperCase());

        // Throw 404 if no role was deleted
        if (affectedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");
        } else {
            return affectedRows;
        }
    }
}
