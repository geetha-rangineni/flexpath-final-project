package support;

/**
 * Represents a login request. This is the request object for the login endpoint.
 */
public class LoginRequest {
    /**
     * The username.
     */
    private String username;

    /**
     * The password.
     */
    private String password;

    /**
     * Default constructor.
     */
    public LoginRequest() {
    }

    /**
     * Constructor.
     *
     * @param username The username.
     * @param password The password.
     */
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Gets the username.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the password.
     *
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the username.
     *
     * @param username The username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the password.
     *
     * @param password The password.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
