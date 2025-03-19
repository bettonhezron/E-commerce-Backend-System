package com.hezron.ecommerce.repository;

import com.hezron.ecommerce.model.Role;
import com.hezron.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRole(Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    long countByIdIn(List<Long> ids);

    // Bulk update status for multiple users

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.active = :active, u.updatedAt = :updatedAt WHERE u.id IN :ids")
    void updateStatusForUsers(@Param("ids") List<Long> ids, @Param("active") boolean active,
                              @Param("updatedAt") LocalDateTime updatedAt);
}
