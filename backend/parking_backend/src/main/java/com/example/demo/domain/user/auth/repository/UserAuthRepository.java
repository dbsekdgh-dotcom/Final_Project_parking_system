package com.example.demo.domain.user.auth.repository;

import com.example.demo.domain.shared.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    Optional<User> findByNameAndPhone(String name, String phone);

    Optional<User> findByNameAndEmailAndPhone(String name, String email, String phone);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.status = 'DELETED', u.deletedAt = CURRENT_TIMESTAMP, u.updatedAt = CURRENT_TIMESTAMP WHERE u.email = :email")
    void withdrawByEmail(@Param("email") String email);
}
