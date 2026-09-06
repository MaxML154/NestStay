package com.neststay.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** JWT 工具类 使用 jjwt 0.12.x + HMAC-SHA256 */
@Component
public class JwtUtil {

  @Value("${jwt.secret:Y29tLm5lc3RzdGF5LXNlY3JldC1rZXktZm9yLWp3dC10b2tlbi0yMDI2LW5lc3RzdGF5}")
  private String secret;

  @Value("${jwt.expiration:86400000}")
  private long expiration; // 默认 24 小时

  /** 生成 token */
  public String generateToken(String userId, String role, String username, String tableName) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", role);
    claims.put("username", username);
    claims.put("tableName", tableName);

    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    return Jwts.builder()
        .claims(claims)
        .subject(userId)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(key)
        .compact();
  }

  /** 从 token 中提取 claims */
  public Claims getClaimsFromToken(String token) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }

  /** 验证 token 是否有效 */
  public boolean validateToken(String token) {
    try {
      getClaimsFromToken(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /** 从 token 获取用户 ID */
  public String getUserIdFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.getSubject();
  }

  /** 从 token 获取角色 */
  public String getRoleFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.get("role", String.class);
  }

  /** 从 token 获取用户名 */
  public String getUsernameFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.get("username", String.class);
  }

  /** 从 token 获取表名 */
  public String getTableNameFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.get("tableName", String.class);
  }
}
