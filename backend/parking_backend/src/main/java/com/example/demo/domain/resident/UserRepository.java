package com.example.demo.domain.resident;

import com.example.demo.domain.resident.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    boolean existsByPhone(String phone);

    // Admin/UserVehicle Page용
    @Query(
            value = """
            SELECT u FROM User u LEFT JOIN FETCH u.household
            WHERE (:keyword IS NULL OR u.name LIKE %:keyword% OR u.email LIKE %:keyword%)
            AND (:status IS NULL OR u.status = :status)
            AND (:isResident IS NULL 
                 OR (:isResident = true AND u.household IS NOT NULL )
                 OR (:isResident = false AND u.household IS NULL ))
            ORDER BY u.createdAt DESC 
            """,
            countQuery = """
            SELECT COUNT(u) FROM User u
            WHERE (:keyword IS NULL OR u.name LIKE %:keyword% OR u.email LIKE %:keyword%)
            AND (:status IS NULL OR u.status = :status)
            AND (:isResident IS NULL 
                 OR (:isResident = true AND u.household IS NOT NULL )
                 OR (:isResident = false AND u.household IS NULL ))   
            """)
    Page<User> findAllForAdmin(
            @Param("keyword") String keyword,
            @Param("status") Status status,
            @Param("isResident") Boolean isResident,
            Pageable pageable
    );


}
