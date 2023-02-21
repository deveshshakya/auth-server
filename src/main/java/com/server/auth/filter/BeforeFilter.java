package com.server.auth.filter;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import com.server.auth.exceptions.InvalidRequestException;
import com.server.auth.utils.IPAddressUtility;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

/** Filter based on IP, to limit requests per minute. */
@Order(HIGHEST_PRECEDENCE)
@Component
public class BeforeFilter implements Filter {
  private final RedisTemplate<String, Long> redisTemplate;
  private final ValueOperations<String, Long> longValueOperations;

  @Value("${hit-limit-per-minute}")
  private Long HIT_LIMIT_PER_IP_PER_MINUTE;

  @Autowired
  public BeforeFilter(@Qualifier("redisForLong") RedisTemplate<String, Long> redisTemplateForLong) {
    this.redisTemplate = redisTemplateForLong;
    this.longValueOperations = redisTemplateForLong.opsForValue();
  }

  @SneakyThrows
  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    String userAgent = request.getHeader("User-Agent");
    String sourceIp = IPAddressUtility.getSourceIP(request);

    if (!StringUtils.isEmpty(userAgent)
        && (userAgent.contains("python-requests") || userAgent.contains("JAVA"))) {
      request.setAttribute("beforeFilter", "invalidSource");
      throw new InvalidRequestException("Invalid source.");
    }

    if (!StringUtils.isEmpty(sourceIp)) {
      if (IPAddressUtility.isValidInet4Address(sourceIp)) {
        Long count = this.longValueOperations.increment(sourceIp);
        if (Long.valueOf(1).equals(count)) {
          this.redisTemplate.expire(sourceIp, Duration.ofMinutes(1));
        }
        if (Objects.equals(count, HIT_LIMIT_PER_IP_PER_MINUTE)) {
          request.setAttribute("beforeFilter", "tooManyRequests");
          throw new InvalidRequestException("Too many requests.");
        }
      }
    }

    filterChain.doFilter(request, response);
  }
}
