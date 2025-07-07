// Required imports for security, DAO, user model, and JWT user implementation
package org.example.services;

import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import org.example.daos.UserDao;
import org.example.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom implementation of Spring Security's UserDetailsService
 * for authenticating users using JWT and loading user roles.
 */
@Component
public class CustomUserDetailsService implements UserDetailsService {

    // Injected DAO to interact with the user table
    private final UserDao userDao;

    /**
     * Constructor-based injection of UserDao.
     */
    public CustomUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Loads user-specific data by username for Spring Security authentication.
     *
     * @param username The username of the user to load.
     * @return UserDetails object with username, password, and authorities.
     * @throws UsernameNotFoundException if user not found in DB.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Retrieve user by username
        User user = userDao.getUserByUsername(username);
        if (user == null) {
            // Throw Spring's exception if user is not found
            throw new UsernameNotFoundException("User not found.");
        }

        // Retrieve list of roles associated with the user (e.g., ROLE_ADMIN)
        List<String> roles = userDao.getRoles(username);

        // Convert roles to Spring Security GrantedAuthority objects
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        // Create a JwtUser object (provided by fraho's security-jwt library)
        JwtUser jwtUser = new JwtUser();
        jwtUser.setUsername(user.getUsername());
        jwtUser.setPassword(user.getPassword());
        jwtUser.setAuthorities(authorities);

        // Set account flags – all are enabled/valid
        jwtUser.setAccountNonExpired(true);
        jwtUser.setAccountNonLocked(true);
        jwtUser.setApiAccessAllowed(true); // Enables API access for JWT flow
        jwtUser.setCredentialsNonExpired(true);
        jwtUser.setEnabled(true);

        return jwtUser; // Return fully constructed Spring-compatible user object
    }
}
