package com.example.demo.domain.user.auth.principal;

import com.example.demo.domain.shared.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * [인증 사용자 정보 객체]
 * Spring Security의 UserDetails와 OAuth2User를 모두 구현하여
 * 일반 로그인과 소셜 로그인 사용자를 통합 관리합니다.
 */
@Getter
public class PrincipalDetails implements UserDetails, OAuth2User {

    // 도메인 모델인 User 엔티티를 직접 들고 있어 서비스 로직에서 유저 정보 접근이 용이합니다.
    private final User user;

    // 소셜 로그인(OAuth2) 시 제공받는 속성값들을 저장합니다.
    private Map<String, Object> attributes;

    // [일반 로그인 생성자]
    public PrincipalDetails(User user){
        this.user = user;
    }

    // [소셜 로그인 생성자]
    public PrincipalDetails(User user, Map<String, Object> attributes){
        this.user = user;
        this.attributes = attributes;
    }

    /**
     * 유저의 고유 식별값(PK) 반환
     * 서비스 레이어에서 userId가 필요할 때 지름길로 사용됩니다.
     */
    public Long getUserId() {
        return user.getUserId();
    }

    // OAuth2User 인터페이스 구현: 소셜 로그인 속성 반환
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // OAuth2User 인터페이스 구현: 사용자의 고유 식별 이름 반환
    @Override
    public String getName() {
        return user.getEmail();
    }

    /**
     * 사용자의 권한 목록 반환
     * 현재는 기본적으로 "ROLE_USER" 권한을 부여합니다.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }

    // 사용자의 비밀번호 반환
    @Override
    public String getPassword(){
        return user.getPassword();
    }

    // 사용자의 아이디(이메일) 반환
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // [계정 상태 검증 메서드들]

    // 계정 만료 여부 (true: 만료 안됨)
    @Override
    public boolean isAccountNonExpired() { return true; }

    // 계정 잠김 여부 (true: 잠기지 않음)
    @Override
    public boolean isAccountNonLocked() { return true; }

    // 비밀번호 만료 여부 (true: 만료 안됨)
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // 계정 활성/비활성 여부 (true: 활성)
    @Override
    public boolean isEnabled() { return true; }
}