package com.server.auth.service;

import com.server.auth.exceptions.TokenRefreshException;
import com.server.auth.model.PrincipleUser;
import com.server.auth.model.RefreshToken;
import com.server.auth.repository.RefreshTokenRepo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JWTService {
  private final RefreshTokenRepo refreshTokenRepo;
  private final ValueOperations<String, String> stringValueOperations;

  @Value("${jwt.secret.key}")
  private String JWT_SECRET_KEY;

  @Value("${jwt.expiry.hours}") // 24 Hrs
  private Integer JWT_EXPIRY_HOUR;

  @Value("${jwt.refreshToke.hours}") // 90 Days
  private Integer JWT_REFRESH_TOKEN_HOUR;

  @Value("${jwt.blacklist.duration.days}") // 1 Day
  private Integer BLACKLIST_TOKEN_DURATION_DAYS;

  @Autowired
  public JWTService(
      RefreshTokenRepo refreshTokenRepo,
      @Qualifier("redisForString") RedisTemplate<String, String> redisTemplate) {
    this.refreshTokenRepo = refreshTokenRepo;
    this.stringValueOperations = redisTemplate.opsForValue();
  }

  public static String jwtFromRequest(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");
    if (!StringUtils.isEmpty(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }
    return null;
  }

  public String generateToken(PrincipleUser principleUser) {
    Map<String, Object> claims = new HashMap<>();
    return this.createToken(claims, principleUser.getUsername());
  }

  public Boolean validateToken(String jwt, UserDetails userDetails) {
    final String userName = extractUserName(jwt);
    return userName.equals(userDetails.getUsername()) && !this.isTokenExpired(jwt);
  }

  // Using mobileNumber as userName
  public String extractUserName(String jwt) {
    return this.extractClaim(jwt, Claims::getSubject);
  }

  public RefreshToken generateRefreshToken(String mobileNumber) {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setMobileNumber(mobileNumber);
    refreshToken.setExpiryDate(
        new Date(System.currentTimeMillis() + (long) 1000 * 60 * 60 * JWT_REFRESH_TOKEN_HOUR));
    refreshToken.setRefreshToken(UUID.randomUUID().toString());
    refreshToken = this.refreshTokenRepo.save(refreshToken);
    return refreshToken;
  }

  public RefreshToken verifyExpiration(RefreshToken token) {
    if (Objects.isNull(token) || Objects.isNull(token.getRefreshToken())) {
      throw new TokenRefreshException("Refresh token is not available");
    }
    if (token.getExpiryDate().compareTo(new Date(System.currentTimeMillis())) < 0) {
      this.refreshTokenRepo.delete(token);
      throw new TokenRefreshException(
          "Refresh token was expired. Please make a new signIn request.");
    }
    return token;
  }

  public RefreshToken findRefreshTokenByToken(String refreshToken) {
    return this.refreshTokenRepo.findByRefreshToken(refreshToken);
  }

  public boolean checkIfJwtIsBlacklisted(String jwt) {
    return "Expired".equals(this.stringValueOperations.get(getTokenBlacklistKey(jwt)));
  }

  public void deleteRefreshToken(String refreshToken, String jwt) {
    Integer response = refreshTokenRepo.deleteByRefreshToken(refreshToken);
    if (Integer.valueOf(0).equals(response)) {
      throw new TokenRefreshException("Already logged out.");
    }
    this.invalidateToken(jwt);
  }

  public void invalidateToken(String jwt) {
    this.stringValueOperations.set(
        getTokenBlacklistKey(jwt), "Expired", Duration.ofDays(BLACKLIST_TOKEN_DURATION_DAYS));
  }

  private String createToken(Map<String, Object> claims, String username) {
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(username)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(
            new Date(System.currentTimeMillis() + (long) 1000 * 60 * 60 * JWT_EXPIRY_HOUR))
        .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY)
        .compact();
  }

  private Boolean isTokenExpired(String token) {
    return this.extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return this.extractClaim(token, Claims::getExpiration);
  }

  private <T> T extractClaim(String jwt, Function<Claims, T> claimsTFunction) {
    final Claims claims = extractAllClaims(jwt);
    return claimsTFunction.apply(claims);
  }

  private Claims extractAllClaims(String jwt) {
    return Jwts.parser().setSigningKey(JWT_SECRET_KEY).parseClaimsJws(jwt).getBody();
  }

  private String getTokenBlacklistKey(String jwt) {
    String BLACKLIST = "BLACKLIST";
    return BLACKLIST + "_" + jwt;
  }
}
