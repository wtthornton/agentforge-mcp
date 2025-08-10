package com.agentforge.repository;

import com.agentforge.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides data access methods for user management operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username or email
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Find active users
     */
    List<User> findByIsActiveTrue();

    /**
     * Find users by role
     */
    List<User> findByRole(User.UserRole role);

    /**
     * Find users by role with pagination
     */
    Page<User> findByRole(User.UserRole role, Pageable pageable);

    /**
     * Find users created after a specific date
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find users who haven't logged in since a specific date
     */
    List<User> findByLastLoginBeforeOrLastLoginIsNull(LocalDateTime date);

    /**
     * Find users by partial username match
     */
    List<User> findByUsernameContainingIgnoreCase(String username);

    /**
     * Find users by partial email match
     */
    List<User> findByEmailContainingIgnoreCase(String email);

    /**
     * Find users by first or last name containing the search term
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByFirstNameOrLastNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    /**
     * Count users by role
     */
    long countByRole(User.UserRole role);

    /**
     * Count active users
     */
    long countByIsActiveTrue();

    /**
     * Find users with pagination and sorting
     */
    Page<User> findAll(Pageable pageable);

    /**
     * Find users by multiple roles
     */
    List<User> findByRoleIn(List<User.UserRole> roles);

    /**
     * Find users by creation date range
     */
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find users by last login date range
     */
    List<User> findByLastLoginBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find users by activity status and role
     */
    List<User> findByIsActiveAndRole(boolean isActive, User.UserRole role);

    /**
     * Find users with pagination, filtering by active status
     */
    Page<User> findByIsActive(boolean isActive, Pageable pageable);

    /**
     * Find users by role with pagination and active status
     */
    Page<User> findByRoleAndIsActive(User.UserRole role, boolean isActive, Pageable pageable);
}
