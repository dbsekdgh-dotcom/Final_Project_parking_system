package com.example.demo.domain.shared.vehicle;

import com.example.demo.domain.shared.vehicle.enums.VehicleStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle,Long> {

    //특정 회원의 활성 차량만 조회
    List<Vehicle> findByUserUserIdAndStatus(Long userId, VehicleStatus status);

}
