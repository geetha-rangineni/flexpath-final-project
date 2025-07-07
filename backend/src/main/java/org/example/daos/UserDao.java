// Package declaration
package org.example.daos;

// Required imports
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.example.exceptions.DaoException;                // Custom exception for DAO failures
import org.example.models.User;                            // User model class
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;         // Spring's JDBC abstraction
import org.springframework.security.crypto.password.PasswordEncoder; // For hashing passwords
import org.springframework.stereotype.Component;

/**
 * DAO class to manage database operations related to users.
 */
@Component
public class UserDao {

    /**
     * Template for executing SQL queries and updates.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Used to securely hash passwords before storing in the database.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor to initialize JdbcTemplate and PasswordEncoder.
     *
     * @param dataSource      The database connection source.
     * @param passwordEncoder Password encoder for hashing user passwords.
     */
    public UserDao(DataSource dataSource, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves all users from the database and attaches their first role.
     *
     * @return List of User objects with roles.
     */
    public List<User> getUsers() {
        List<User> users = jdbcTemplate.query("SELECT * FROM users ORDER BY username", this::mapToUser);

        // Attach the first role (if any) to each user
        for (User user : users) {
            List<String> roles = getRoles(user.getUsername());
            if (!roles.isEmpty()) {
                user.setRole(roles.get(0));
            }
        }

        return users;
    }

    /**
     * Retrieves a single user by username.
     *
     * @param username The username to search for.
     * @return User object or null if not found.
     */
    public User getUserByUsername(String username) {
        try {
            User user = jdbcTemplate.queryForObject(
                "SELECT * FROM users WHERE username = ?", this::mapToUser, username
            );

            // Attach role if available
            List<String> roles = getRoles(user.getUsername());
            if (!roles.isEmpty()) {
                user.setRole(roles.get(0));
            }

            return user;
        } catch (EmptyResultDataAccessException e) {
            return null; // User not found
        }
    }

    /**
     * Creates a new user account with a hashed password and assigns a role.
     *
     * @param user The user to create.
     * @return The newly created user with the role.
     */
    public User createUser(User user) {
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        String sql = "INSERT INTO users (username, password) VALUES (?, ?);";

        try {
            jdbcTemplate.update(sql, user.getUsername(), hashedPassword);
            addRole(user.getUsername(), user.getRole()); // Add role after creation
            return getUserByUsername(user.getUsername());
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Failed to create user.");
        }
    }

    /**
     * Updates the password of an existing user.
     *
     * @param user The user whose password will be updated.
     * @return The updated User object.
     */
    public User updatePassword(User user) {
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        int rowsAffected = jdbcTemplate.update(sql, hashedPassword, user.getUsername());

        if (rowsAffected == 0) {
            throw new DaoException("Zero rows affected, expected at least one.");
        }

        return getUserByUsername(user.getUsername());
    }

    /**
     * Deletes a user by username.
     *
     * @param username The username of the user to delete.
     * @return The number of rows affected.
     */
    public int deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        return jdbcTemplate.update(sql, username);
    }

    /**
     * Retrieves all roles associated with a specific user.
     *
     * @param username The username to fetch roles for.
     * @return List of roles.
     */
    public List<String> getRoles(String username) {
        return jdbcTemplate.queryForList(
            "SELECT role FROM roles WHERE username = ?;", String.class, username
        );
    }

    /**
     * Assigns a new role to the user. Converts the role to uppercase before storing.
     *
     * @param username The username to assign the role to.
     * @param role     The role to assign.
     * @return Updated list of roles.
     */
    public List<String> addRole(String username, String role) {
        try {
            String sql = "INSERT INTO roles (username, role) VALUES (?, ?)";
            jdbcTemplate.update(sql, username, role);
        } catch (DataAccessException e) {
            // Swallow exception silently (not recommended—should ideally log or rethrow)
        }
        return getRoles(username);
    }

    /**
     * Removes a specific role from the user.
     *
     * @param username The username whose role should be removed.
     * @param role     The role to remove.
     * @return Number of rows affected.
     */
    public int deleteRole(String username, String role) {
        String sql = "DELETE FROM roles WHERE username = ? AND role = ?";
        return jdbcTemplate.update(sql, username, role);
    }

    /**
     * Maps a single row of the ResultSet to a User object.
     *
     * @param resultSet The result set from the database query.
     * @param rowNumber The index of the current row.
     * @return A User object.
     * @throws SQLException If the result set access fails.
     */
    private User mapToUser(ResultSet resultSet, int rowNumber) throws SQLException {
        String username = resultSet.getString("username");
        String password = resultSet.getString("password");
        return new User(username, password);
    }
}
