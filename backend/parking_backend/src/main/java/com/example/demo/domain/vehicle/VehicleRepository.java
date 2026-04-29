package com.example.demo.domain.vehicle;

import com.example.demo.domain.vehicle.enums.VehicleStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle,Long> {

    //특정 회원의 활성 차량만 조회
    List<Vehicle> findByUser_UserIdAndStatus(Long userId, VehicleStatus status);


    Optional<Vehicle> findByCarNumber(String carNumber);


    @Query("SELECT v FROM Vehicle v WHERE v.user.userId = :userId AND v.status != com.example.demo.domain.vehicle.enums.VehicleStatus.DELETED")
    Optional<Vehicle> findCurrentVehicle(@Param("userId") Long userId);

    //윤진 추가
    @Query("SELECT v FROM Vehicle v WHERE v.user.userId =:userId AND v.status = :status ORDER BY v.createdAt DESC LIMIT 1")
    Optional<Vehicle> findMainVehicle(@Param("userId") Long userId, @Param("status") VehicleStatus status);

    // Admin/UserVehicle Page용

    @Query(
            value = """
            SELECT v FROM Vehicle v LEFT JOIN FETCH v.user u
            WHERE v.status != 'PENDING'
            AND (:keyword IS NULL OR v.carNumber LIKE %:keyword% OR (u IS NOT NULL AND u.name LIKE %:keyword%))
            AND (:status IS NULL OR v.status = :status)
            ORDER BY v.createdAt DESC 
            """,
            countQuery = """
            SELECT COUNT(v) FROM Vehicle v LEFT JOIN v.user u
            WHERE v.status != 'PENDING'
            AND (:keyword IS NULL OR v.carNumber LIKE %:keyword% OR (u IS NOT NULL AND u.name LIKE %:keyword%))
            AND (:status IS NULL OR v.status = :status)
            """)
    Page<Vehicle> findAllForAdmin(
            @Param("keyword") String keyword,
            @Param("status") VehicleStatus status,
            Pageable pageable
    );
    @Query("SELECT v FROM Vehicle v LEFT JOIN FETCH v.user u WHERE v.id = :id")
    Optional<Vehicle> findByIdWithUser(@Param("id") Long id);

    List<Vehicle> findByUser_UserId(Long userUserId);

    //Admin Dashbaord 사용량
    long countByStatus(VehicleStatus status);

}
