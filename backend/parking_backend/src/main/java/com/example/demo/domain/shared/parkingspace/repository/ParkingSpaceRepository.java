package com.example.demo.domain.shared.parkingspace.repository;

import com.example.demo.domain.shared.parkingspace.ParkingSpace;
import com.example.demo.domain.shared.parkingspace.enums.Floor;
import com.example.demo.domain.shared.parkingspace.enums.SpaceStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {

    // 층별 전체 주차공간 조회 (관리자 화면)
    List<ParkingSpace> findByFloorOrderBySpaceCodeAsc(Floor floor);

    // 층별 + 특정 상태 제외 조회 (키오스크 공간 선택)
    List<ParkingSpace> findByFloorAndStatusNot(Floor floor, SpaceStatus status);

    // 상태별 카운트
    long countByStatus(SpaceStatus status);

    long countByFloor(Floor floor);

    long countByFloorAndStatus(Floor floor, SpaceStatus status);

    Optional<ParkingSpace> findByIdAndStatus(Long id, SpaceStatus status);

    @Query("""
        SELECT COUNT(ps)
        FROM ParkingSpace ps
        WHERE ps.status = 'AVAILABLE'
          AND ps.isDisabled = false
        """)
    long countAvailableSpace();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ps FROM ParkingSpace ps WHERE ps.id = :id")
    Optional<ParkingSpace> findByIdWithLock(@Param("id") Long id);
}
