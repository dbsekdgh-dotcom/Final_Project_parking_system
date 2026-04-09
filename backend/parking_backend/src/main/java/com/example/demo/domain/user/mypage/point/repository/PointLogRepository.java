package com.example.demo.domain.user.mypage.point.repository;

import com.example.demo.domain.user.mypage.point.entity.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PointLogRepository extends JpaRepository<PointLog,Long> {

    //특정 유저의 포인트 이력 조회(최신순)
    List<PointLog> findByUserUserIdOrderByCreatedAtDesc(Long userId);


}
