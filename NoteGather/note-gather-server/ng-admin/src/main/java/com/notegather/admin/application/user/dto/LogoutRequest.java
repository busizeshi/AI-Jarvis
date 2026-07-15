package com.notegather.admin.application.user.dto;

import lombok.Data;

@Data
public class LogoutRequest {

    private String refreshToken;
}
