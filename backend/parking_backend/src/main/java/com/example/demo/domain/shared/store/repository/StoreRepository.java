package com.example.demo.domain.shared.store.repository;

import com.example.demo.domain.shared.store.Store;
import com.example.demo.domain.shared.store.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store,Long> {
    @Query("select s from Store s where s.name like %:keyword% and s.status=:status order by s.storeId asc ")
    List<Store> findAdminStoreByKeyword(@Param("keyword") String keyword, @Param("status")Status status);
}
