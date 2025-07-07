// Package declaration
package support;

// Required imports
import org.apache.ibatis.jdbc.ScriptRunner; // Utility to execute SQL scripts
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * Base test class for integration testing of the Web Store application.
 * Provides utilities for database reset, authentication, and HTTP interactions.
 */
public class WebStoreTest {

    /**
     * Injected dynamic port for test server (randomized at runtime).
     */
    @LocalServerPort
    protected int port;

    /**
     * Encrypted bcrypt password for 'admin' used in tests.
     * (Use this value when pre-inserting users into the database).
     */
    public String encryptedAdminPassword = "$2a$10$yNhRmtAD2o/E/5CH83yGsO2aoC3ww1JUE76xUrIYLbfNcTV5G2WrO";

    /**
     * Injected TestRestTemplate used to perform REST calls to the test server.
     */
    @Autowired
    protected TestRestTemplate restTemplate;

    /**
     * Injected DataSource to run SQL operations or get DB connections.
     */
    @Autowired
    protected DataSource dataSource;

    /**
     * Injected PasswordEncoder for hashing and comparing passwords.
     */
    @Autowired
    protected PasswordEncoder passwordEncoder;

    /**
     * Returns the base URL of the running test server (e.g., http://localhost:8081).
     */
    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }

    /**
     * Executes a raw SQL statement using a database connection.
     *
     * @param sql SQL string to execute.
     * @throws SQLException if execution fails.
     */
    protected void executeSql(String sql) throws SQLException {
        try (var statement = dataSource.getConnection().createStatement()) {
            statement.execute(sql);
        }
    }

    /**
     * Creates a JdbcTemplate using the test DataSource.
     * Useful for performing direct SQL operations in tests.
     */
    protected JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Prepares the test environment before each test case runs.
     * Loads the schema/data using `create-database.sql`.
     *
     * @throws SQLException if DB connection fails.
     * @throws IOException if SQL script can't be read.
     */
    @BeforeEach
    public void setUp() throws SQLException, IOException {
        var connection = dataSource.getConnection();
        var reader = new java.io.InputStreamReader(
            WebStoreTest.class.getResource("/create-database.sql").openStream()
        );

        // Run SQL script using MyBatis ScriptRunner
        var sr = new ScriptRunner(connection);
        sr.setStopOnError(true);
        sr.setLogWriter(null);         // Suppress logs
        sr.setErrorLogWriter(null);    // Suppress error logs
        sr.runScript(reader);          // Executes SQL statements in the file
        connection.close();
    }

    /**
     * Authenticates using the provided username and password,
     * and returns an HttpEntity with a Bearer token (no body).
     *
     * @param username Username for login.
     * @param password Password for login.
     * @return HttpEntity with Authorization header set.
     */
    protected HttpEntity<Object> GetAuthEntity(String username, String password) {
        var user = new LoginRequest(username, password);

        var loginResult = this.restTemplate.postForEntity(
            "http://localhost:" + port + "/auth/login",
            user,
            LoginResponse.class
        );

        if (loginResult.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to login: " + loginResult.getStatusCode());
        }

        // Extract token from response
        var token = Objects.requireNonNull(loginResult.getBody()).getAccessToken().getToken();

        // Build headers with Bearer token
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        return new HttpEntity<>(headers); // Entity with headers only
    }

    /**
     * Same as above, but includes a request body.
     * Useful for sending authenticated POST/PUT requests.
     *
     * @param <T> Type of the request body.
     * @param username Username for login.
     * @param password Password for login.
     * @param body Request body to include.
     * @return HttpEntity with Authorization and body.
     */
    protected <T> HttpEntity<T> GetAuthEntity(String username, String password, T body) {
        var user = new LoginRequest(username, password);

        var loginResult = this.restTemplate.postForEntity(
            "http://localhost:" + port + "/auth/login",
            user,
            LoginResponse.class
        );

        assertEquals(HttpStatus.OK, loginResult.getStatusCode());

        // Extract access token
        var token = Objects.requireNonNull(loginResult.getBody()).getAccessToken().getToken();

        // Build headers with Bearer token
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        return new HttpEntity<>(body, headers); // Entity with body and headers
    }
}
