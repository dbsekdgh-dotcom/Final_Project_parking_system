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

    //carNumber로 차량 아이디 조회
    @Query("select v.id from Vehicle v where v.carNumber=:carNumber and v.status=:status")
    Optional<Long> getVehicleIdByCarNumber(@Param("carNumber") String carNumber, @Param("status") VehicleStatus status);
}
