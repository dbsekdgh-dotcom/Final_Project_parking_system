package com.example.demo.api.admin.auth;

import com.example.demo.global.redis.RedisService;
import com.example.demo.global.util.admin.AdminJWTException;
import com.example.demo.global.util.admin.AdminJWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@Tag(name = "1. 인증 (Auth)", description = "관리자 JWT 토큰 갱신 및 로그아웃 API")
@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/admin")
public class AdminAuthController {
    private final AdminJWTUtil adminJWTUtil;
    private final RedisService redisService;

    @Operation(summary = "Access 토큰 갱신", description = "만료된 Access 토큰을 Refresh 토큰 쿠키로 재발급합니다. Refresh 토큰이 1시간 미만 남았으면 쿠키도 함께 갱신됩니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/refresh")
    public Map<String,Object> refresh(@RequestHeader(value = HttpHeaders.AUTHORIZATION,required = false)String authHeader,
                                      HttpServletRequest request,
                                      HttpServletResponse response){
        log.info("----------- [Admin Token Refresh] 시작 -----------");

        // 쿠키에서 refreshToken 추출
        String refreshToken = null;
        if(request.getCookies() != null){
            refreshToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> "refreshToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        // 기본 검증(토큰이 비어있는지 등)
        if (refreshToken==null)throw new RuntimeException("NULL_REFRESH");
        if (authHeader==null || authHeader.length() < 7)throw new RuntimeException("INVALID_HEADER");

        String accessToken = authHeader.substring(7);

        // Access 토큰이 아직 살아있다면 그대로 반환
        if(!isExpired(accessToken)){
            log.info("Access토큰이 아직 유효함. 기존 토큰 유지.");
            return Map.of("accessToken",accessToken);
        }
        // Access토큰이 만료되었다면 Refresh토큰 검증 및 클레임 추출
        Claims claims = adminJWTUtil.validateToken(refreshToken);
        String loginId = (String) claims.get("loginId");

        // Redis에 저장된 토큰과 대조
        String savedToken = redisService.getRefreshToken(loginId);
        if(savedToken == null || !savedToken.equals(refreshToken)){
            log.warn("토큰 불일치!!! 탈취 의심 혹은 로그아운된 세션: {}", loginId);
            throw new RuntimeException("INVALID_REFRESH_IN_REDIS");
        }

        // 새로운 Access토큰 발급(30분)
        String newAccessToken = adminJWTUtil.generateToken(Map.of(
                "loginId",claims.get("loginId"),
                "name",claims.get("name"),
                "role","ROLE_ADMIN"
        ),30);
        // Refresh토큰도 만료 임박(1시간 미만)했다면 쿠키 갱신
        if(checkTime((Long) claims.get("exp"))){
            log.info("Refresh 토큰 만료 임박. 새 쿠키 발급 중...");
            String newRefreshToken = adminJWTUtil.generateToken(Map.of(
                    "loginId",claims.get("loginId"),
                    "name",claims.get("name")
            ), 60 * 24); // 24시간으로 갱신

            // Redis에 새로운 토큰 업데이트
            redisService.saveRefreshToken(loginId, newRefreshToken, 60*24);

            ResponseCookie newCookie = ResponseCookie.from("refreshToken",newRefreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(24*60*60)
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, newCookie.toString());
        }
        return Map.of("accessToken",newAccessToken);
    }

    private boolean isExpired(String token){
        try {
            adminJWTUtil.validateToken(token);
            return false;
        }catch (Exception e){
            return true; // 예외가 발생하면 만료되었거나 문제가 있는 토큰으로 간주
        }
    }

    private boolean checkTime(Long exp){
        long expMillis = exp * 1000L; //밀리세컨즈 단위로 들어가야하기때문에 *1000
        //현재 시간과의 차이 계산 - 밀리세컨즈
        long gap = expMillis - System.currentTimeMillis();
        //분단위 계산
        long leftMin = gap / (1000 * 60);
        //1시간도 안남았는지..
        return leftMin < 60;
    }

    @Operation(summary = "로그아웃", description = "Redis에서 Refresh 토큰을 삭제하고 쿠키를 즉시 만료시킵니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/logout")
    public Map<String, String> adminLogout(HttpServletRequest request, HttpServletResponse response){
        String loginId =null;
        //헤더에서 토큰을 꺼내 직접 아이디 추출
        String headerAuth = request.getHeader("Authorization");
        if(headerAuth!=null && headerAuth.startsWith("Bearer ")){
            String accessToken = headerAuth.substring(7);
            try{
                //정상 토큰일때 ID 추출
                Claims claims = adminJWTUtil.validateToken(accessToken);
                loginId = (String) claims.get("loginId");
            }catch (AdminJWTException e){
                //만료된 토큰일때
                log.info("만료된 토큰으로 로그아웃 시도 중... ID 추출 시도");
                try {
                    // 만료된 토큰이라도 서명(Key)만 맞으면 내부 Payload(Claims)를 강제로 읽을 수 있음
                    loginId = adminJWTUtil.getAdminLoginIdWithoutValidation(accessToken);
                }catch (Exception ex){
                    log.error("만료 토큰에서 ID 추출 실패: {}",ex.getMessage());
                }
            } catch (Exception e){
                log.error("로그아웃 토큰 파싱 실패: {}", e.getMessage());
            }
        }

        log.info("----------- [Admin Logout] 최종 확인 ID: {} -----------", loginId);

        //ID가 확보되면 Redis에서 삭제
        if(loginId!=null){
            redisService.deleteRefreshToken(loginId);
            log.info("Redis RefreshToken 삭제 완료: {}", loginId);
        }
        // 브라우저의 쿠키 무효화 (Max-Age를 0으로 설정) - ID유무과 상관없음
        ResponseCookie cookie = ResponseCookie.from("refreshToken","")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) //즉시만료
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE,cookie.toString());
        return Map.of("result","success");
    }
}
