package com.student.expensetracker.security;

import com.student.expensetracker.entity.User;
import com.student.expensetracker.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Resolves users by email for the login flow (PRD 4.1).
 *
 * <p>Deliberately NOT a {@code @Service} bean: Spring Security warns when both
 * a {@code UserDetailsService} bean and an {@code AuthenticationProvider} bean
 * exist, because it cannot tell which one you meant. It is instantiated
 * explicitly inside SecurityConfig's provider instead.
 */
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("No account for " + email));
        return new UserPrincipal(user);
    }
}