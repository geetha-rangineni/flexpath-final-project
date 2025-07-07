// Required imports for testing, HTTP operations, and Spring Boot test setup
import org.example.SpringBootApplication;
import org.example.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import support.FinalTestConfiguration;
import support.WebStoreTest;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for the /api/users endpoints,
 * including authorization, CRUD operations, and edge cases.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = SpringBootApplication.class)
@Import(FinalTestConfiguration.class)
public class UserEndpointTests extends WebStoreTest {

    /**
     * Tests unauthorized access to /api/users without a token.
     */
    @Test
    @DisplayName("GET /api/users should return a 401 if not authorized")
    public void getUsersShouldFailIfNotAuthorized() {
        var result = this.restTemplate.getForEntity(getBaseUrl() + "/api/users", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    /**
     * Tests forbidden access to /api/users for a regular user.
     */
    @Test
    @DisplayName("GET /api/users should return a 403 if not an admin")
    public void getUsersShouldFailIfUserNotAdmin() throws SQLException {
        getJdbcTemplate().update("insert into users (username, password) values ('user', 'user')");
        var requestEntity = GetAuthEntity("user", "user");
        var result = this.restTemplate.exchange(getBaseUrl() + "/api/users", HttpMethod.GET, requestEntity, String.class);
        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
    }

    /**
     * Tests successful admin access to /api/users.
     */
    @Test
    @DisplayName("GET /api/users should return a 200 and a list of one user if authorized")
    public void getUsersShouldSucceedIfAuthorized() {
        var requestEntity = GetAuthEntity("test-admin", "admin");
        var result = this.restTemplate.exchange(getBaseUrl() + "/api/users", HttpMethod.GET, requestEntity, User[].class);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("test-admin", result.getBody()[4].getUsername());
    }

    /**
     * Tests unauthorized access to /api/users/{username}.
     */
    @Test
    @DisplayName("GET /api/users/{username} should return a 401 if not authorized")
    public void getUserShouldFailIfNotAuthorized() {
        var result = this.restTemplate.getForEntity(getBaseUrl() + "/api/users/test-admin", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    /**
     * Tests forbidden access to /api/users/{username} for a non-admin user.
     */
    @Test
    @DisplayName("GET /api/users/{username} should return a 403 if not an admin")
    public void getUserShouldFailIfUserNotAdmin() throws SQLException {
        getJdbcTemplate().update("insert into users (username, password) values ('user', 'user')");
        var requestEntity = GetAuthEntity("user", "user");
        var result = this.restTemplate.exchange(getBaseUrl() + "/api/users/test-admin", HttpMethod.GET, requestEntity, String.class);
        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
    }

    /**
     * Tests successful retrieval of a user by admin.
     */
    @Test
    @DisplayName("GET /api/users/{username} should return a 200 and the user if authorized")
    public void getUserShouldSucceedIfAuthorized() {
        var requestEntity = GetAuthEntity("test-admin", "admin");
        var result = this.restTemplate.exchange(getBaseUrl() + "/api/users/test-admin", HttpMethod.GET, requestEntity, User.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("test-admin", result.getBody().getUsername());
    }

    /**
     * Tests user registration without authentication.
     */
    @Test
    @DisplayName("POST /api/users should return a 201 and the created user if not authorized")
    public void createUserShouldSucceedIfNotAuthorized() {
        var result = this.restTemplate.postForEntity(getBaseUrl() + "/api/users", new User("user", "user"), User.class);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals("user", result.getBody().getUsername());
    }

    /**
     * Tests user registration with authentication.
     */
    @Test
    @DisplayName("POST /api/users should return a 201 and the created user if authorized")
    public void createUserShouldSucceedIfAuthorized() {
        var requestEntity = GetAuthEntity("test-admin", "admin", new User("user", "user"));
        var result = this.restTemplate.exchange(getBaseUrl() + "/api/users", HttpMethod.POST, requestEntity, User.class);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals("user", result.getBody().getUsername());
    }

    /**
     * Tests password update without authentication.
     */
    @Test
    @DisplayName("PUT /api/users/{username}/password should return a 401 if not authorized")
    public void updatePasswordShouldFailIfNotAuthorized() {
        var result = this.restTemplate.exchange(
            getBaseUrl() + "/api/users/test-admin/password",
            HttpMethod.PUT,
            new HttpEntity<>(new User("test-admin", "admin")),
            String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    /**
     * Tests forbidden password update by a regular user.
     */
    @Test
    @DisplayName("PUT /api/users/{username}/password should return a 403 if not an admin")
    public void updatePasswordShouldFailIfUserNotAdmin() throws SQLException {
        getJdbcTemplate().update("insert into users (username, password) values ('user', 'user')");
        var requestEntity = GetAuthEntity("user", "user", "test");
        var result = this.restTemplate.exchange(
            getBaseUrl() + "/api/users/test-admin/password",
            HttpMethod.PUT,
            requestEntity,
            String.class
        );
        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
    }

    /**
     * Tests successful password update by admin.
     */
    @Test
    @DisplayName("PUT /api/users/{username}/password should return a 200 and update the user if authorized")
    public void updatePasswordShouldSucceedIfAuthorized() {
        var requestEntity = GetAuthEntity("test-admin", "admin", "test");

        var result = this.restTemplate.exchange(
            getBaseUrl() + "/api/users/test-admin/password",
            HttpMethod.PUT,
            requestEntity,
            User.class
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());

        var updatedUser = getJdbcTemplate().queryForObject(
            "select * from users where username = 'test-admin'",
            (rs, rowNum) -> new User(rs.getString("username"), rs.getString("password"))
        );

        assertEquals("test-admin", updatedUser.getUsername());
        assertEquals("test", updatedUser.getPassword());
    }

    /**
     * Tests unauthorized user deletion.
     */
    @Test
    @DisplayName("DELETE /api/users/{username} should return a 401 if not authorized")
    public void deleteUserShouldFailIfNotAuthorized() {
        getJdbcTemplate().update("insert into users (username, password) values ('user', 'user')");
        var result = this.restTemplate.exchange(
            getBaseUrl() + "/api/users/user",
            HttpMethod.DELETE,
            new HttpEntity<>(new HttpHeaders()),
            String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    /**
     * Tests forbidden deletion by a non-admin user.
     */
    @Test
    @DisplayName("DELETE /api/users/{username} should return a 403 if not an admin")
    public void deleteUserShouldFailIfUserNotAdmin() throws SQLException {
        getJdbcTemplate().update("insert into users (username, password) values ('user', 'user')");
        var requestEntity = GetAuthEntity("user", "user");
        var result = this.restTemplate.exchange(
            getBaseUrl() + "/api/users/user",
            HttpMethod.DELETE,
            requestEntity,
            String.class
        );
        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
    }

    /**
     * Tests successful user deletion by admin.
     */
    @Test
    @DisplayName("DELETE /api/users/{username} should return a 204 and delete the user if authorized")
    public void deleteUserShouldSucceedIfAuthorized() {
        getJdbcTemplate().update("insert into users (username, password) values ('user', 'user')");
        var requestEntity = GetAuthEntity("test-admin", "admin");

        var result = this.restTemplate.exchange(
            getBaseUrl() + "/api/users/user",
            HttpMethod.DELETE,
            requestEntity,
            Integer.class
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody());

        // Confirm user is deleted from DB
        var user = getJdbcTemplate().query(
            "select * from users where username = 'user'",
            (rs, rowNum) -> new User(rs.getString("username"), rs.getString("password"))
        );
        assertEquals(0, user.size());
    }
}
