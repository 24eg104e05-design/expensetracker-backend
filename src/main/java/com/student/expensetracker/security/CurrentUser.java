package com.student.expensetracker.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Single source of truth for "who is calling". Services take the id from here
 * rather than from any request payload, which is what enforces PRD 4.1 isolation.
 */
@Component
public class CurrentUser {

    public UserPrincipal require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthenticatedException("No authenticated user in context");
        }
        return principal;
    }

    public Long requireId() {
        return require().getId();
    }
}