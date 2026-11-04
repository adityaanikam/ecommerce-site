package com.ecommerce.auth.repository;

import com.ecommerce.auth.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    /**
     * Matched on the lowercased column so it uses uq_users_email_lower and so
     * "A@B.com" finds the account registered as "a@b.com".
     */
    @Query("SELECT u FROM UserAccount u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<UserAccount> findByEmailIgnoreCase(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM UserAccount u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);
}
