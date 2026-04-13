package com.example.demo.domain.shared.store.repository;

import com.example.demo.domain.shared.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store,Long> {
}
