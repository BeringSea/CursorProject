package com.example.person.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponseDto {
    private String apiPath;
    private int errorCode;
    private String errorMessage;
    private LocalDateTime errorTime;
}
