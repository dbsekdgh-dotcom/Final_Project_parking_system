package com.example.demo.domain.system.store.transaction.repository;

import com.example.demo.domain.dashboard.dtos.response.UsageDailyProjection;
import com.example.demo.domain.system.store.transaction.StoreTicketTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StoreTicketTransactionRepository extends JpaRepository<StoreTicketTransaction, Long> {

    List<StoreTicketTransaction> findByStore_StoreIdOrderByCreatedAtDesc(Long storeId);

    @Query("SELECT COUNT(t) FROM StoreTicketTransaction t WHERE t.transactionType =" +
            "com.example.demo.domain.system.store.transaction.enums.TransactionType.USE " +
            "AND t.createdAt BETWEEN :start AND :end")
    long countUsedBetween(@Param("start")LocalDateTime start,
                          @Param("end")LocalDateTime end);

    @Query(value =
            "SELECT DATE_FORMAT(t.created_at, '%Y-%m-%d') AS date," +
                    " '할인권' AS category," +
                    " SUM(t.quantity) AS usageCount," +
                    " COUNT(*) AS transactionCount " +
                    "FROM store_ticket_transaction t " +
                    "WHERE t.transaction_type = 'USE' " +
                    "AND t.created_at BETWEEN :from AND :to " +
                    "GROUP BY DATE_FORMAT(t.created_at, '%Y-%m-%d') " +
                    "ORDER BY DATE_FORMAT(t.created_at, '%Y-%m-%d') DESC",
            nativeQuery = true)
    List<UsageDailyProjection> findDailyUsedRows(@Param("from") LocalDateTime from,
                                                 @Param("to") LocalDateTime to);
}
