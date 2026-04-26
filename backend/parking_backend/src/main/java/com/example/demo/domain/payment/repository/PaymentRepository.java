package com.example.demo.domain.payment.repository;

import com.example.demo.domain.parking.log.ParkingLog;
import com.example.demo.domain.payment.Payment;
import com.example.demo.domain.payment.enums.PaymentType;
import com.example.demo.domain.payment.statistics.dtos.response.DailyDetailDto;
import com.example.demo.domain.payment.statistics.dtos.response.SummaryStatsDto;
import com.example.demo.domain.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.domain.payment.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByExternalPaymentId(String externalPaymentId);

    Optional<Payment> findFirstByExternalPaymentId(String externalPaymentId);

    //특정 주차 로그에 대해 READY / FAILED 상태인 결제내역만 조회
    List<Payment> findAllByParkingLogAndPaymentStatusIn(ParkingLog parkingLog, Collection<PaymentStatus> paymentStatus);
    //반대로 SUCCESS, CANCELLED, REFUNDED가 아닌것만 조회
    List<Payment> findAllByParkingLogAndPaymentStatusNotIn(ParkingLog parkingLog, Collection<PaymentStatus> statuses);
    //ParkingLog 엔티티과 결제 상태를 조건으로 리스트 조회
    List<Payment> findAllByParkingLogAndPaymentStatus(ParkingLog parkingLog,PaymentStatus paymentStatus);

    //요약 매출 정보
    @Query(value = "select " +
            "coalesce(sum(p.amount),0) as revenue, " +
            "coalesce(sum(p.refunded_amount),0) as refund, " +
            "coalesce(sum(case when p.payment_method=:payMethod then p.amount else 0 end),0) as payAmount, " +
            "coalesce(sum(case when p.payment_method=:pointMethod then p.amount else 0 end),0) as pointAmount, " +
            "coalesce(sum(case when p.payment_method=:pointMethod then p.refunded_amount else 0 end),0) as pointRefund, " +
            "coalesce(sum(case when p.payment_method=:payMethod then p.refunded_amount else 0 end),0) as payRefund " +
            "from payment p " +
            "where p.payment_status in (:successStatus,:refundedStatus) and p.paid_at between :start and :end ", nativeQuery = true)
    SummaryStatsDto summaryStats(@Param("start") LocalDateTime start,@Param("end") LocalDateTime end,
                                       @Param("payMethod") String payMethod, @Param("pointMethod") String pointMethod,
                                 @Param("successStatus") String successStatus,@Param("refundedStatus") String refundedStatus);


    //날짜별 매출정보
    @Query(value = "select " +
            "date(p.paid_at) as date, " +
            "sum(case when p.payment_type=:parkingType then p.amount else 0 end) as parkingRevenue, " +
            "sum(case when p.payment_type=:ticketType then p.amount else 0 end) as ticketRevenue, " +
            "sum(case when p.payment_type=:subscriptionType then p.amount else 0 end) as subscriptionRevenue," +
            "sum(p.refunded_amount) as refund," +
            "sum(case when p.payment_method=:payMethod then p.amount else 0 end) as payAmount, " +
            "sum(case when p.payment_method=:payMethod then p.refunded_amount else 0 end) as payRefund, " +
            "sum(case when p.payment_method=:pointMethod then p.amount else 0 end) as pointAmount, " +
            "sum(case when p.payment_method=:pointMethod then p.refunded_amount else 0 end) as pointRefund " +
            "from payment p " +
            "where p.payment_status in (:successStatus,:refundedStatus) and p.paid_at between :start and :end " +
            "group by date(p.paid_at)", nativeQuery = true)
    List<DailyDetailDto> dailyStats(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end,
                                    @Param("parkingType") String parkingType,
                                    @Param("ticketType") String ticketType,
                                    @Param("subscriptionType") String subscriptionType,
                                    @Param("payMethod") String payMethod,
                                    @Param("pointMethod") String pointMethod,
                                    @Param("successStatus") String successStatus,
                                    @Param("refundedStatus") String refundedStatus);


    //parking log 결제 내역
    @Query("select p from Payment p where p.parkingLog.parkingLogId in :parkingLogIds and p.paymentStatus in :paymentStatus")
    List<Payment> findPaymentsByParkinglogId(@Param("parkingLogIds") List<Long> parkingLogIds, @Param("paymentStatus") List<PaymentStatus> paymentStatus);

    //Admin Dashboard 사용량
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentType = :type AND p.paymentStatus = :status")
    long sumAmountByPaymentTypeAndStatus(@Param("type")PaymentType type,
                                         @Param("status") PaymentStatus status);
}
