package com.example.demo.global.security.store;

import com.example.demo.global.util.admin.AdminJWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@RequiredArgsConstructor
public class KioskJwtFilter extends OncePerRequestFilter {
    private final AdminJWTUtil adminJWTUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return !path.startsWith("/api/store") || path.equals("/api/store/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header==null || !header.startsWith("Bearer ")){
            sendError(response, "STORE_TOKEN_MISSING");
            return;
        }

        String token = header.substring(7);
        try{
            var authentication = adminJWTUtil.getKioskAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request,response);
        }catch (Exception e){
            sendError(response, "INVALID_STORE_TOKEN");
        }
    }

    private void sendError(HttpServletResponse response, String message) throws IOException{
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        pw.println("{\"error\": \""+message+"\"}");
        pw.close();
    }
}
