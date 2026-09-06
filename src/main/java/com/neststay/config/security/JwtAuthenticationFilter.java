package com.neststay.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/** JWT 认证过滤器 从请求头提取 token，验证并设置 SecurityContext 替换原 AuthorizationInterceptor */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Autowired private JwtUtil jwtUtil;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String token = extractTokenFromHeader(request);

    if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
      String userId = jwtUtil.getUserIdFromToken(token);
      String role = jwtUtil.getRoleFromToken(token);

      // 设置 Spring Security 认证上下文
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              userId, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
      authentication.setDetails(request);

      SecurityContextHolder.getContext().setAuthentication(authentication);

      String username = jwtUtil.getUsernameFromToken(token);
      String tableName = jwtUtil.getTableNameFromToken(token);

      // 兼容旧版代码：设置 session 属性（原 AuthorizationInterceptor 行为）
      request.getSession().setAttribute("userId", Long.parseLong(userId));
      request.getSession().setAttribute("role", role);
      request.getSession().setAttribute("username", username);
      request.getSession().setAttribute("tableName", tableName);
    } else {
      // 无有效 token 时清掉旧 session 身份，避免未登录请求仍能读到订单
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.removeAttribute("userId");
        session.removeAttribute("role");
        session.removeAttribute("username");
        session.removeAttribute("tableName");
      }
    }

    filterChain.doFilter(request, response);
  }

  /** 从请求头提取 Bearer token */
  private String extractTokenFromHeader(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    // 兼容旧版：直接从 token 头获取
    String oldToken = request.getHeader("token");
    if (StringUtils.hasText(oldToken)) {
      return oldToken;
    }
    return null;
  }
}
