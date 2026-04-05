package com.example.demo.domain.shared.parkingTicket.repository;

import com.example.demo.domain.shared.parkingTicket.ParkingTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParkingTicketRepository extends JpaRepository<ParkingTicket,Long> {
    @Query("select t.parkingTicketId,t.parkingLog.parkingLogId,t.ticketPolicy from ParkingTicket t " +
            "join fetch t.ticketPolicy where t.parkingLog.parkingLogId=:parkingLogId")
    List<ParkingTicket> getParkingTicketByParkingLogId(@Param("parkingLogId") Long parkingLogId);
}
