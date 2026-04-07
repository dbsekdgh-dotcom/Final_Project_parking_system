package com.example.demo.domain.user.auth.repository;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAuthRepository extends JpaRepository<User, Long> {

    // 1. 기본 조회 및 중복 체크 (SignupService 빨간 줄 해결용)
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // 2. [복구용] 특정 상태의 유저 존재 여부 체크
    // 복구 시, 원본 번호로 이미 ACTIVE 상태인 유저가 있는지 확인하기 위함
    boolean existsByPhoneAndStatus(String phone, Status status);

    // 3. [복구용] 탈퇴 상태인 유저를 이메일로 찾기
    Optional<User> findByEmailAndStatus(String email, Status status);

    // 4. [아이디 찾기]
    @Query("SELECT u FROM User u WHERE u.name = :name AND u.phone LIKE CONCAT(:phone, '%') ORDER BY u.status ASC")
    List<User> findByNameAndPhoneForFindingEmail(@Param("name") String name, @Param("phone") String phone);

    // 5. [비밀번호 찾기용]
    @Query("SELECT u FROM User u WHERE u.name = :name AND u.email = :email AND u.phone LIKE CONCAT(:phone, '%')")
    Optional<User> findByNameAndEmailAndPhoneForReset(@Param("name") String name, @Param("email") String email, @Param("phone") String phone);

    /**
     * [탈퇴 처리]
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET " +
            "u.status = com.example.demo.domain.shared.user.enums.Status.DELETED, " +
            "u.phone = CONCAT(u.phone, '_', CURRENT_TIMESTAMP), " +
            "u.deletedAt = CURRENT_TIMESTAMP, " +
            "u.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE u.email = :email AND u.status = com.example.demo.domain.shared.user.enums.Status.ACTIVE")
    void withdrawByEmail(@Param("email") String email);

    /**
     * [복구 처리]
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET " +
            "u.status = com.example.demo.domain.shared.user.enums.Status.ACTIVE, " +
            "u.phone = :rawPhone, " +
            "u.deletedAt = null, " +
            "u.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE u.email = :email AND u.status = com.example.demo.domain.shared.user.enums.Status.DELETED")
    void recoverByEmail(@Param("email") String email, @Param("rawPhone") String rawPhone);
    /**
     * [계정 복구용] 이름, 이메일, 전화번호가 일치하는 '탈퇴 유저' 조회
     */
    @Query("SELECT u FROM User u WHERE u.name = :name AND u.email = :email " +
            "AND u.phone LIKE CONCAT(:phone, '%') " +
            "AND u.status = com.example.demo.domain.shared.user.enums.Status.DELETED")
    Optional<User> findDeletedUserForRecovery(@Param("name") String name,
                                              @Param("email") String email,
                                              @Param("phone") String phone);
    /**
     * [복구 처리 + 비밀번호 재설정]
     * 상태를 ACTIVE로 돌리고, 번호를 원복하며, 새로운 비밀번호로 교체합니다.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET " +
            "u.status = com.example.demo.domain.shared.user.enums.Status.ACTIVE, " +
            "u.phone = :rawPhone, " +
            "u.password = :encodedPassword, " +
            "u.deletedAt = null, " +
            "u.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE u.email = :email AND u.status = com.example.demo.domain.shared.user.enums.Status.DELETED")
    void recoverByEmailWithPassword(@Param("email") String email,
                                    @Param("rawPhone") String rawPhone,
                                    @Param("encodedPassword") String encodedPassword);
}