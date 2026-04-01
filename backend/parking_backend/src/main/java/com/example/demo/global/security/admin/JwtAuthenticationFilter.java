package com.example.demo.global.security.admin;

import com.example.demo.global.util.admin.AdminJWTUtil;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AdminJWTUtil adminJWTUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
        String path = request.getRequestURI();
        log.info("----------- [Admin JWT Filter] Checking path: "+path+" -----------");

        //로그인과 리프레시 경로는 이 필터를 타지 않고 바로 컨트롤러로 보냄.
        if(path.startsWith("/admin/login") || path.startsWith("/admin/refresh")){
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                 FilterChain filterChain) throws ServletException, IOException{
        //1. 헤더에서 Authorization값을 가져옴
        String headerAuth=request.getHeader("Authorization");
        log.info("----------- [Admin JWT Filter] Authorization Header: "+headerAuth+" -----------");
        //2. 토큰이 없거나 "Bearer "로 시작하지 않으면 다음 필터로 진행(인증 미처리)
        if(headerAuth==null || !headerAuth.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        //3. "Bearer "뒷부분의 실제 토큰 추출
        String accessToken=headerAuth.substring(7);

        try {
            //4. 토큰 검증 및 내부 데이터(Claims) 추출
            Claims claims=adminJWTUtil.validateToken(accessToken);
            log.info("----------- [Admin JWT Filter] Token Validated. Claims: "+claims+" -----------");
            //5. 토큰 정보를 바탕으로 AdminAuthDto 객체 생성
            String loginId = (String) claims.get("loginId");
            String name = (String) claims.get("name");
            AdminAuthDto adminAuthDto = new AdminAuthDto(loginId,"pw_hidden",name);
            //6. 스프링 시큐리티 전용 인증 토큰 생성
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(adminAuthDto,null,adminAuthDto.getAuthorities());
            //7. 시큐리티 메모리(Contex)에 "이 사람 인증됨"이라고 기록
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            //8. 다음 필터로 이동
            filterChain.doFilter(request,response);
        }catch (Exception e){
            // 토큰이 만료되었거나 변조된 경우 에러 처리
            log.error("----------- [Admin JWT Filter] Token Error: "+e.getMessage()+" -----------");
            //1. 응답 설정 / 토큰이 잘못되었을 때는 보통 401 에러임
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            //2. Gson을 사용하여 JSON 메시지 생성
            Gson gson=new Gson();
            String jsonStr = gson.toJson(Map.of("error","ERROR_ACCESS_TOKEN"));
            //3. 응답 전송
            PrintWriter pw = response.getWriter();
            pw.println(jsonStr);
            pw.close();

        }
    }
}
