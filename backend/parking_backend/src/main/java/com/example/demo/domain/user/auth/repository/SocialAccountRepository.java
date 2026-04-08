package com.example.demo.domain.user.auth.repository;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.user.entity.SocialAccount;
import com.example.demo.domain.user.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderId(Provider provider, String providerId);

    List<SocialAccount> findByUser(User user);

    boolean existsByUser(User user);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM SocialAccount s WHERE s.user.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
