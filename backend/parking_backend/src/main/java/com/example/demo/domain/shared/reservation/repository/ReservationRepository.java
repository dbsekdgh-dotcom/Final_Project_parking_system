package com.example.demo.domain.shared.reservation.repository;

import com.example.demo.domain.shared.reservation.Reservation;
import com.example.demo.domain.shared.reservation.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    //방문예약자가 입차&요금정책&출차시간 이내 출차인지
    @Query("select count(r.reservationId) from Reservation r where r.carNumber=:carNumber and r.status=:status and r.visitEndAt>current_timestamp and r.isFree=:isFree")
    int getCountbyCarNumber(@Param("carNumber") String carNumber, @Param("status") Status status, @Param("isFree") Boolean isFree);

    //isFree = false 업데이트 쿼리
    @Modifying
    @Transactional
    @Query("UPDATE Reservation r SET r.isFree = false WHERE r.carNumber = :carNumber AND r.status= 'ENTERED'")
    void updateIsFreeByCarNumber(@Param("carNumber") String carNumber);

    //status = ENTERED 업데이트 쿼리
    @Modifying
    @Transactional
    @Query("UPDATE Reservation r SET r.status = 'ENTERED' WHERE r.carNumber = :carNumber AND r.status = 'RESERVED' AND current_timestamp BETWEEN r.visitStartAt AND r.visitEndAt")
    void updateStatusToEntered(@Param("carNumber") String carNumber);
}
