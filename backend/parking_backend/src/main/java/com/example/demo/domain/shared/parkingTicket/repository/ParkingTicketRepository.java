package com.example.demo.domain.shared.parkingTicket.repository;

import com.example.demo.domain.kiosk.payment.dtos.request.DiscountTicketRequestDto;
import com.example.demo.domain.shared.parkingTicket.ParkingTicket;
import com.example.demo.domain.shared.ticketPolicy.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParkingTicketRepository extends JpaRepository<ParkingTicket,Long> {
    @Query("select t from ParkingTicket t " +
            "left join fetch t.ticketPolicy where t.parkingLog.parkingLogId=:parkingLogId and t.ticketPolicy.status=:status")
    List<ParkingTicket> getValidTickets(@Param("parkingLogId") Long parkingLogId, @Param("status") Status status);
}
