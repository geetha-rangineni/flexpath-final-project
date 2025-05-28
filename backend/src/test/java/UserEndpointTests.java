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
 * Tests for the user endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = SpringBootApplication.class)
@Import(FinalTestConfiguration.class)
public class UserEndpointTests extends WebStoreTest {
    /**
     * Tests that getting users fails if not authorized.
     */
    @Test
    @DisplayName("GET /api/users should return a 401 if not authorized")
    public void getUsersShouldFailIfNotAuthorized() {
        var result = this.restTemplate.getForEntity(getBaseUrl() + "/api/users", String.class);
        var responseCode = result.getStatusCode();
        assertEquals(HttpStatus.UNAUTHORIZED, responseCode);
    }

    /**
     * Tests that getting users fails if not an admin.
     */
    @Test
    @DisplayName("GET /api/users should return a 403 if not an admin")
    public void getUsersShouldFailIfUserNotAdmin() throws SQLException {
        getJdbcTemplate().update("insert into users (username, password) values ('user', 'user')");
        var requestEntity = GetAuthEntity("user", "user");
        var result = this.restTemplate.exchange(getBaseUrl() + "/api/users", HttpMethod.GET, requestEntity, String.class);
        var responseCode = result.getStatusCode();
        assertEquals(HttpStatus.FORBIDDEN, responseCode);
    }

    /**
     * Tests that getting users succeeds if authorized.
     */
    @Test
    @DisplayName("GET /api/users should return a 200 and a list of one user if authorized")
    public void getUsersShouldSucceedIfAuthorized() {
        var requestEntity = GetAuthEntity("test-admin", "admin");
        var result = this.restTemplate.exchange(getBaseUrl() + "/api/users", HttpMethod.GET, requestEntity, User[].class);
        var responseCode = result.getStatusCode();
        var users = result.getBody();
        assertEquals(HttpStatus.OK, responseCode);
        assertEquals(5, users.length);
        assertEquals("test-admin", users[4].getUsername());
    }

    /**
     * Tests that getting a user by username fails if not authorized.
     */
    @Test
    @DisplayName("GET /api/users/{username} should return a 401 if not authorized")
    public void getUserShouldFailIfNotAuthorized() {
        var result = this.restTemplate.getForEntity(getBaseUrl() + "/api/users/test-admin", String.class);
        var responseCode = result.getStatusCode();
        assertEquals(HttpStatus.UNAUTHORIZED, responseCode);
    }

    /**
     * Tests that getting a user by username fails if not an admin.
     */
    @Test
    @DisplayName("GET /api/users/{username} should return a 403 if not an admin")
    public void getUserShouldFailIfUserNotAdmin() throws SQLException {
        getJdbcTemplate().update("insert into users (username, password) values ('user', 'user')");
        var requestEntity = GetAuthEntity("user", "user");
        var result = this.restTemplate.exchange(getBaseUrl() + "/api/users/test-admin", HttpMethod.GET, requestEntity, String.class);
        var responseCode = result.getStatusCode();
        assertEquals(HttpStatus.FORBIDDEN, responseCode);
    }

    /**
     * Tests that getting a user by username succeeds if authorized.
     */
    @Test
    @DisplayName("GET /api/users/{username} should return a 200 and the user if authorized")
    public void getUserShouldSucceedIfAuthorized() {
        var requestEntity = GetAuthEntity("test-admin", "admin");
        var result = this.restTemplate.exchange(getBaseUrl() + "/api/users/test-admin", HttpMethod.GET, requestEntity, User.class);
        var responseCode = result.getStatusCode();
        var user = result.getBody();
        assertEquals(HttpStatus.OK, responseCode);
        assertEquals("test-admin", user.getUsername());
    }

    /**
     * Tests that creating a user succeeds if not authorized.
     */
    @Test
    @DisplayName("POST /api/users should return a 201 and the created user if not authorized")
    public void createUserShouldSucceedIfNotAuthorized() {
        var result = this.restTemplate.postForEntity(getBaseUrl() + "/api/users", new User("user", "user"), User.class);
        var responseCode = result.getStatusCode();
        var createdUser = result.getBody();
        assertEquals(HttpStatus.CREATED, responseCode);
        assertEquals("user", createdUser.getUsername());
        assertEquals("user", createdUser.getPassword());
    }

    /**
     * Tests that creating a user succeeds if authorized.
     */
    @Test
    @DisplayName("POST /api/users should return a 201 and the created user if authorized")
    public void createUserShouldSucceedIfAuthorized() {
        var requestEntity = GetAuthEntity("test-admin", "admin", new User("user", "user"));
        var result = this.restTemplate.exchange(getBaseUrl() + "/api/users", HttpMethod.POST, requestEntity, User.class);
        var responseCode = result.getStatusCode();
        var createdUser = result.getBody();
        assertEquals(HttpStatus.CREATED, responseCode);
        assertEquals("user", createdUser.getUsername());
        assertEquals("user", createdUser.getPassword());
    }

    /**
     * Tests that updating a user's password fails if not authorized.
     */
    @Test
    @DisplayName("PUT /api/users/{username}/password should return a 401 if not authorized")
    public void updatePasswordShouldFailIfNotAuthorized() {
        var result = this.restTemplate.exchange(getBaseUrl() + "/api/users/test-admin/password", HttpMethod.PUT, new HttpEntity<>(new User("test-admin", "admin")), String.class);
        var responseCode = result.getStatusCode();
        assertEquals(HttpStatus.UNAUTHORIZED, responseCode);
    }

    /**
     * Tests that updating a user's password fails if not an admin.
     */
    @Test
    @DisplayName("PUT /api/users/{username}/password should return a 403 if not an admin")
    public void updatePasswordShouldFailIfUserNotAdmin() throws SQLException {
        getJdbcTemplate().update("insert into users (username, password) values ('user', 'user')");
        var requestEntity = GetAuthEntity("user", "user", "test");
        var result = this.restTemplate.exchange(getBaseUrl() + "/api/users/test-admin/password", HttpMethod.PUT, requestEntity, String.class);
        var responseCode = result.getStatusCode();
        assertEquals(HttpStatus.FORBIDDEN, responseCode);
    }

    /**
     * Tests that updating a user's password succeeds if authorized.
     */
    @Test
    @DisplayName("PUT /api/users/{username}/password should return a 200 and update the user if authorized")
    public void updatePasswordShouldSucceedIfAuthorized() {
        var requestEntity = GetAuthEntity("test-admin", "admin", "test");

        var result = this.restTemplate.exchange(getBaseUrl() + "/api/users/test-admin/password", HttpMethod.PUT, requestEntity, User.class);
        var responseCode = result.getStatusCode();
        assertEquals(HttpStatus.OK, responseCode);

        var updatedUser = getJdbcTemplate().queryForObject("select * from users where username = 'test-admin'", (rs, rowNum) -> {
            var user = new User(rs.getString("username"), rs.getString("password"));
            return user;
        });

        assertEquals("test-admin", updatedUser.getUsername());
        assertEquals("test", updatedUser.getPassword());
    }

    /**
     * Tests that deleting a user fails if not authorized.
     */
    @Test
    @DisplayName("DELETE /api/users/{username} should return a 401 if not authorized")
    public void deleteUserShouldFailIfNotAuthorized() {
        getJdbcTemplate().update("insert into users (username, password) values ('user', 'user')");
        var result = this.restTemplate.exchange(getBaseUrl() + "/api/users/user", HttpMethod.DELETE, new HttpEntity<>(new HttpHeaders()), String.class);
        var responseCode = result.getStatusCode();
        assertEquals(HttpStatus.UNAUTHORIZED, responseCode);
    }

    /**
     * Tests that deleting a user fails if not an admin.
     */
    @Test
    @DisplayName("DELETE /api/users/{username} should return a 403 if not an admin")
    public void deleteUserShouldFailIfUserNotAdmin() throws SQLException {
        getJdbcTemplate().update("insert into users (username, password) values ('user', 'user')");
        var requestEntity = GetAuthEntity("user", "user");
        var result = this.restTemplate.exchange(getBaseUrl() + "/api/users/user", HttpMethod.DELETE, requestEntity, String.class);
        var responseCode = result.getStatusCode();
        assertEquals(HttpStatus.FORBIDDEN, responseCode);
    }

    /**
     * Tests that deleting a user succeeds if authorized.
     */
    @Test
    @DisplayName("DELETE /api/users/{username} should return a 204 and delete the user if authorized")
    public void deleteUserShouldSucceedIfAuthorized() {
        getJdbcTemplate().update("insert into users (username, password) values ('user', 'user')");
        var requestEntity = GetAuthEntity("test-admin", "admin");
        var result = this.restTemplate.exchange(getBaseUrl() + "/api/users/user", HttpMethod.DELETE, requestEntity, Integer.class);
        var responseCode = result.getStatusCode();
        assertEquals(HttpStatus.OK, responseCode);
        var body = result.getBody();
        assertEquals(1, body);

        var user = getJdbcTemplate().query("select * from users where username = 'user'", (rs, rowNum) -> {
            var u = new User(rs.getString("username"), rs.getString("password"));
            return u;
        });
        assertEquals(0, user.size());
    }
}
