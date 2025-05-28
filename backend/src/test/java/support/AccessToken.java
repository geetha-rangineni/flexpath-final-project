package support;

/**
 * Represents an access token container. This is a submember of the response object
 * from the login endpoint. (Contained in the accessToken field of the LoginResponse object)
 */
public class AccessToken {
    /**
     * The internal token.
     */
    private String token;

    /**
     * Default constructor.
     */
    public AccessToken() {
    }

    /**
     * Constructor.
     *
     * @param token The internal token.
     */
    public AccessToken(String token) {
        this.token = token;
    }

    /**
     * Gets the internal token.
     *
     * @return The internal token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the internal token.
     *
     * @param token The internal token.
     */
    public void setToken(String token) {
        this.token = token;
    }
}
