package com.example.demo.domain.shared.vehicle.repository;

import com.example.demo.domain.shared.vehicle.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    @Query("select v.id from Vehicle v where v.carNumber=:carNumber")
    Optional<Long> getVehicleIdByCarNumber(String carNumber);
}
