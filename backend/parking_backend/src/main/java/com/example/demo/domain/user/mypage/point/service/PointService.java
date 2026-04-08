package com.example.demo.domain.user.mypage.point.service;

import com.example.demo.domain.shared.payment.Payment;
import com.example.demo.domain.shared.payment.repository.PaymentRepository;
import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.UserRepository;
import com.example.demo.domain.user.mypage.point.dto.PointLogDto;
import com.example.demo.domain.user.mypage.point.dto.PointResponseDto;
import com.example.demo.domain.user.mypage.point.entity.PointLog;
import com.example.demo.domain.user.mypage.point.entity.PointReason;
import com.example.demo.domain.user.mypage.point.entity.UserPoint;
import com.example.demo.domain.user.mypage.point.repository.PointLogRepository;
import com.example.demo.domain.user.mypage.point.repository.UserPointRepository;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointRepository userPointRepository;
    private final PointLogRepository pointLogRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    //1.현재 포인트 조회
    public PointResponseDto getUserPoint(Long userId){

        UserPoint userPoint = userPointRepository.findByUserUserId(userId)
                .orElseThrow(()-> new CustomException(ErrorCode.POINT_NOT_FOUND));

        //포인트 이력 조회
        List<PointLog> logs=pointLogRepository.findByUserUserIdOrderByCreatedAtDesc(userId);

        //Entity -> DTO 변환
        List<PointLogDto> history = logs.stream()
                .map(log->new PointLogDto(
                        log.getChangeAmount(),
                        log.getBeforePoint(),
                        log.getAfterPoint(),
                        log.getReason().name(),  //ENUM → String 변환
                        log.getDescription(),
                        log.getCreatedAt()
                ))
                .toList();

        //응답 DTO생성
        return new PointResponseDto(userPoint.getCurrentPoint(),history);
    }

    //2. 포인트 적립
    @Transactional
    public void earnPoints(Long userId, Long paymentId, int amount, String description){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_NOT_ENOUGH));

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(()-> new CustomException(ErrorCode.POINT_INVALID_REQUEST));

        UserPoint userPoint = userPointRepository.findByUserUserId(userId)
                .orElseThrow(()->new CustomException(ErrorCode.POINT_NOT_ENOUGH));

        int before = userPoint.getCurrentPoint();
        int after = before + amount;

        //현재 포인트 갱신
        userPoint.setCurrentPoint(after);
        userPointRepository.save(userPoint);

        //포인트 로그 기록
        PointLog log = PointLog.builder()
                .user(user)
                .payment(payment)
                .changeAmount(amount)
                .beforePoint(before)
                .afterPoint(after)
                .reason(PointReason.PAYMENT_EARN)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        pointLogRepository.save(log);
    }

    //3. 포인트 사용
    @Transactional
    public void usePoints(Long userId, Long paymentId, int amount, String description){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_NOT_ENOUGH));

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(()->new CustomException(ErrorCode.POINT_INVALID_REQUEST));

        UserPoint userPoint = userPointRepository.findByUserUserId(userId)
                .orElseThrow(()->new CustomException(ErrorCode.POINT_NOT_ENOUGH));

        int before = userPoint.getCurrentPoint();

        if (before < amount) {
            throw new CustomException(ErrorCode.POINT_NOT_ENOUGH);
        }
        int after = before - amount;

        //현재 포인트 갱신
        userPoint.setCurrentPoint(after);
        userPointRepository.save(userPoint);

        //포인트 로그 기록
        PointLog log = PointLog.builder()
                .user(user)
                .payment(payment)
                .changeAmount(-amount) //사용은 음수
                .beforePoint(before)
                .afterPoint(after)
                .reason(PointReason.PAYMENT_USE)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        pointLogRepository.save(log);
    }
}
