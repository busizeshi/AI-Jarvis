package com.notegather.common.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "ng.security.user-context")
public class UserContextProperties {

    private List<String> whiteList = new ArrayList<>(List.of(
            "/api/v1/user/register",
            "/api/v1/user/login",
            "/api/v1/user/refresh-token",
            "/actuator/**"
    ));
}
