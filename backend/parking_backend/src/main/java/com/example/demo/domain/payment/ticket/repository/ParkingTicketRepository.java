package com.example.demo.domain.payment.ticket.repository;

import com.example.demo.domain.payment.dtos.request.DiscountTicketRequestDto;
import com.example.demo.domain.payment.ticket.ParkingTicket;
import com.example.demo.domain.parking.log.ParkingLog;
import com.example.demo.domain.payment.ticketpolicy.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParkingTicketRepository extends JpaRepository<ParkingTicket,Long> {
    List<ParkingTicket> findAllByParkingLog(ParkingLog parkingLog);

    @Query("select t from ParkingTicket t " +
            "left join fetch t.ticketPolicy where t.parkingLog.parkingLogId=:parkingLogId and t.ticketPolicy.status=:status")
    List<ParkingTicket> getValidTickets(@Param("parkingLogId") Long parkingLogId, @Param("status") Status status);
}
