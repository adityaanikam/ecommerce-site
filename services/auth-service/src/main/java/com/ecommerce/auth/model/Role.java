package com.ecommerce.auth.model;

/**
 * The two roles the system has. Kept as an enum rather than a free-text column so a
 * typo cannot invent a third one, and mirrored by a CHECK constraint in V1 so the
 * database will not accept one either.
 */
public enum Role {
    USER,
    ADMIN
}
