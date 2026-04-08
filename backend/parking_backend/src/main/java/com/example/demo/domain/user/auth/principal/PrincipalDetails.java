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

@Getter
public class PrincipalDetails implements UserDetails, OAuth2User {

    private final User user;
    private Map<String, Object> attributes;

    // 일반 로그인 시 사용되는 생성자
    public PrincipalDetails(User user){
        this.user = user;
    }

    // 소셜 로그인 시 사용되는 생성자
    public PrincipalDetails(User user, Map<String, Object> attributes){
        this.user = user;
        this.attributes = attributes;
    }

    /**
     * ⭐ 추가: 유저의 PK(userId)를 편리하게 가져오기 위한 메서드
     * 컨트롤러에서 principalDetails.getUserId()로 바로 호출 가능합니다.
     */
    public Long getUserId() {
        return user.getUserId();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 현재 유저의 권한을 반환 (기본 ROLE_USER 설정)
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword(){
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // 계정 만료 여부 (true: 만료 안됨)
    @Override
    public boolean isAccountNonExpired() { return true; }

    // 계정 잠김 여부 (true: 잠기지 않음)
    @Override
    public boolean isAccountNonLocked() { return true; }

    // 비밀번호 만료 여부 (true: 만료 안됨)
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // 계정 활성화 여부 (true: 활성)
    @Override
    public boolean isEnabled() { return true; }
}