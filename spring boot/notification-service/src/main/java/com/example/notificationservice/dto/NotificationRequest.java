package com.example.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Subject is required")
    private String subject;

    private String message;
}
