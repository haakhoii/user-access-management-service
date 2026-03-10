package com.r2s.auth.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class AuthenticationRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes servletRequestAttribute =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String authHeader = servletRequestAttribute.getRequest().getHeader("Authorization");
        log.info("Auth Header: {}", authHeader);

        if (StringUtils.hasText(authHeader)) requestTemplate.header("Authorization", authHeader);
    }
}
