package com.example.demo.domain.user.entity;

import com.example.demo.domain.user.enums.Provider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "social_account",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_provider",
                        columnNames = {"provider", "provider_id"}
                )
        }
)
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_account_id")
    private Long socialAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private Provider provider;

    @Column(name = "provider_id", nullable = false, length = 150)
    private String providerId;

    @Column(name = "connected_at", nullable = false, updatable = false)
    private LocalDateTime connectedAt;

    @Builder
    public SocialAccount(User user, Provider provider, String providerId) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
        this.connectedAt = LocalDateTime.now();
    }


}
