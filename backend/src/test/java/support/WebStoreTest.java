package support;

import org.apache.ibatis.jdbc.ScriptRunner;
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
 * Base class for web store tests.
 */
public class WebStoreTest {
    /**
     * The port of the server.
     */
    @LocalServerPort
    protected int port;

    /**
     * The encrypted password "admin"..
     */
    public String encryptedAdminPassword = "$2a$10$yNhRmtAD2o/E/5CH83yGsO2aoC3ww1JUE76xUrIYLbfNcTV5G2WrO";

    /**
     * An injected REST template for use in tests.
     */
    @Autowired
    protected TestRestTemplate restTemplate;

    /**
     * An injected data source for use in tests.
     */
    @Autowired
    protected DataSource dataSource;

    /**
     * An injected password encoder for use in tests.
     */
    @Autowired
    protected PasswordEncoder passwordEncoder;

    /**
     * Gets the base URL for the server.
     *
     * @return The base URL for the server.
     */
    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }

    /**
     * Executes the given SQL statement.
     *
     * @param sql The SQL statement.
     * @throws SQLException If an error occurs while executing the SQL statement.
     */
    protected void executeSql(String sql) throws SQLException {
        try(var statement = dataSource.getConnection().createStatement()) {
            statement.execute(sql);
        }
    }

    /**
     * Gets a JDBC template for the data source.
     *
     * @return The JDBC template.
     */
    protected JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Sets up the test environment.
     *
     * @throws SQLException If an error occurs while setting up the database.
     * @throws IOException If an error occurs while reading the database creation script.
     */
    @BeforeEach
    public void setUp() throws SQLException, IOException {
        var connection = dataSource.getConnection();
        var reader = new java.io.InputStreamReader(
            WebStoreTest.class.getResource
            ("/create-database.sql").openStream()
        );
        var sr = new ScriptRunner(connection);
        sr.setStopOnError(true);
        sr.setLogWriter(null);
        sr.setErrorLogWriter(null);
        sr.runScript(reader);
        connection.close();
    }

    /**
     * Gets an empty HTTP entity with a bearer token retrieved from the auth endpoint
     * using the given username and password.
     *
     * @param username The username.
     * @param password The password.
     * @return The HTTP entity.
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
        var token = Objects.requireNonNull(loginResult.getBody()).getAccessToken().getToken();
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

    /**
     * Gets an HTTP entity with a bearer token retrieved from the auth endpoint
     * using the given username and password and the given body.
     *
     * @param <T> The type of the body.
     * @param username The username.
     * @param password The password.
     * @param body The body.
     * @return The HTTP entity.
     */
    protected <T> HttpEntity<T> GetAuthEntity(String username, String password, T body) {
        var user = new LoginRequest(username, password);
        var loginResult = this.restTemplate.postForEntity(
            "http://localhost:" + port + "/auth/login",
            user,
            LoginResponse.class
        );

        assertEquals(HttpStatus.OK, loginResult.getStatusCode());
        var token = Objects.requireNonNull(loginResult.getBody()).getAccessToken().getToken();
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
