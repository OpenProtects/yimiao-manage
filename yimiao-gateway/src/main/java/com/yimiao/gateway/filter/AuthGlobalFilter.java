package com.yimiao.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.yimiao.common.core.Result;
import com.yimiao.common.core.ResultCode;
import com.yimiao.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/user/login",
            "/api/user/register",
            "/api/user/logout",
            "/api/sms/**",
            "/api/vaccine/available",
            "/api/vaccine/page",
            "/api/vaccine/*",
            "/api/site/all",
            "/api/site/page",
            "/api/site/*",
            "/api/slot/available",
            "/api/slot/page",
            "/api/stock/available",
            "/api/payment/channels",
            "/api/blacklist/check/*",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v2/api-docs/**",
            "/actuator/**"
    );

    public AuthGlobalFilter() {
        this.redisTemplate = null;
        this.jwtUtil = new JwtUtil();
        try {
            java.lang.reflect.Field secretField = JwtUtil.class.getDeclaredField("secret");
            secretField.setAccessible(true);
            secretField.set(this.jwtUtil, "yimiao-vaccine-appointment-system-secret-key-2024");
            
            java.lang.reflect.Field expirationField = JwtUtil.class.getDeclaredField("expiration");
            expirationField.setAccessible(true);
            expirationField.set(this.jwtUtil, 86400000L);
            
            java.lang.reflect.Field prefixField = JwtUtil.class.getDeclaredField("prefix");
            prefixField.setAccessible(true);
            prefixField.set(this.jwtUtil, "Bearer ");
            
            java.lang.reflect.Field headerField = JwtUtil.class.getDeclaredField("header");
            headerField.setAccessible(true);
            headerField.set(this.jwtUtil, "Authorization");
            
            java.lang.reflect.Method initMethod = JwtUtil.class.getDeclaredMethod("init");
            initMethod.setAccessible(true);
            initMethod.invoke(this.jwtUtil);
        } catch (Exception e) {
            log.error("初始化JwtUtil失败", e);
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isWhitePath(path)) {
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(token)) {
            return unauthorized(exchange, "未登录");
        }

        token = jwtUtil.extractToken(token);
        if (token == null || !jwtUtil.validateToken(token)) {
            return unauthorized(exchange, "Token无效或已过期");
        }

        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        Integer userType = jwtUtil.getUserType(token);

        if (userId == null) {
            return unauthorized(exchange, "Token解析失败");
        }

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-Username", username != null ? username : "")
                .header("X-User-Type", userType != null ? String.valueOf(userType) : "0")
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isWhitePath(String path) {
        for (String whitePath : WHITE_LIST) {
            if (pathMatcher.match(whitePath, path)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), message);
        String body = JSON.toJSONString(result);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
