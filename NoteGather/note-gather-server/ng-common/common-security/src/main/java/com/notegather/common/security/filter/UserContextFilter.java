package com.notegather.common.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notegather.common.core.constant.CommonConstants;
import com.notegather.common.core.result.Result;
import com.notegather.common.core.result.ResultCode;
import com.notegather.common.security.config.UserContextProperties;
import com.notegather.common.security.context.UserContext;
import com.notegather.common.security.model.LoginUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class UserContextFilter extends OncePerRequestFilter {

    private final UserContextProperties properties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            if (!isWhiteListed(request.getRequestURI())) {
                LoginUser loginUser = buildLoginUser(request);
                if (loginUser == null) {
                    writeUnauthorized(response);
                    return;
                }
                UserContext.set(loginUser);
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private boolean isWhiteListed(String path) {
        return properties.getWhiteList().stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private LoginUser buildLoginUser(HttpServletRequest request) {
        String userId = request.getHeader(CommonConstants.HEADER_USER_ID);
        if (!isPositiveLong(userId)) {
            return null;
        }
        return LoginUser.builder()
                .userId(userId)
                .username(request.getHeader(CommonConstants.HEADER_USERNAME))
                .accessTokenJti(request.getHeader(CommonConstants.HEADER_TOKEN_JTI))
                .accessTokenExpiresAt(parseLong(request.getHeader(CommonConstants.HEADER_TOKEN_EXPIRES_AT)))
                .build();
    }

    private boolean isPositiveLong(String value) {
        Long parsed = parseLong(value);
        return parsed != null && parsed > 0;
    }

    private Long parseLong(String value) {
        try {
            return value == null ? null : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Result.fail(ResultCode.UNAUTHORIZED));
    }
}
