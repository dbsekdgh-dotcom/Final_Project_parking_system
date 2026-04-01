package com.example.demo.domain.kiosk.exit.repository;

import com.example.demo.domain.shared.parkinglog.ParkingLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExitRepository extends JpaRepository<ParkingLog,Long> {

}
