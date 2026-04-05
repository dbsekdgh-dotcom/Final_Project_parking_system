package com.example.demo.domain.user.mypage.point.service;

import com.example.demo.domain.user.mypage.point.dto.PointLogDto;
import com.example.demo.domain.user.mypage.point.dto.PointResponseDto;
import com.example.demo.domain.user.mypage.point.entity.PointLog;
import com.example.demo.domain.user.mypage.point.entity.UserPoint;
import com.example.demo.domain.user.mypage.point.repository.PointLogRepository;
import com.example.demo.domain.user.mypage.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointRepository userPointRepository;
    private final PointLogRepository pointLogRepository;

    public PointResponseDto getUserPoint(Long userId){

        //1.현재 포인트 조회
        UserPoint userPoint = userPointRepository.findByUserUserId(userId)
                .orElseThrow(()-> new IllegalArgumentException("포인트 정보 없음"));

        //2. 포인트 이력 조회
        List<PointLog> logs=pointLogRepository.findByUserUserIdOrderByCreatedAtDesc(userId);

        //3. Entity -> DTO 변환
        List<PointLogDto> history = logs.stream()
                .map(log->new PointLogDto(
                        log.getChangAmount(),
                        log.getBeforePoint(),
                        log.getAfterPoint(),
                        log.getReason().name(),  //ENUM → String 변환
                        log.getDescription(),
                        log.getCreatedAt()
                ))
                .toList();

        //4. 응답 DTO생성
        return new PointResponseDto(
                userPoint.getCurrentPoint(),
                history
        );


    }
}
