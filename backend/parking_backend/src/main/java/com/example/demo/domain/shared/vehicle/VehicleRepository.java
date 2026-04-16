package com.example.demo.domain.shared.vehicle;

import com.example.demo.domain.shared.vehicle.enums.VehicleStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle,Long> {

    //특정 회원의 활성 차량만 조회
    List<Vehicle> findByUser_UserIdAndStatus(Long userId, VehicleStatus status);

    //윤진 추가
    @Query("SELECT v FROM Vehicle v WHERE v.user.userId =:userId AND v.status = :status ORDER BY v.createdAt DESC LIMIT 1")
    Optional<Vehicle> findMainVehicle(@Param("userId") Long userId, @Param("status") VehicleStatus status);
}
