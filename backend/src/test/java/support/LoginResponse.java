package support;

/**
 * Represents a login response. This is the response object for the login endpoint.
 */
public class LoginResponse {
    /**
     * The access token.
     */
    private AccessToken accessToken;

    /**
     * Default constructor.
     */
    public LoginResponse() {
    }

    /**
     * Constructor.
     *
     * @param accessToken The access token.
     */
    public LoginResponse(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Gets the access token.
     *
     * @return The access token.
     */
    public AccessToken getAccessToken() {
        return accessToken;
    }

    /**
     * Sets the access token.
     *
     * @param accessToken The access token.
     */
    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }
}