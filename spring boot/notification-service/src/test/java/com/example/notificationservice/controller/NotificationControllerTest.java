package com.example.notificationservice.controller;

import com.example.notificationservice.dto.NotificationRequest;
import com.example.notificationservice.dto.NotificationResponse;
import com.example.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSendNotification() throws Exception {
        NotificationRequest request = new NotificationRequest(1L, "Test Message", "email");
        NotificationResponse response = new NotificationResponse(1L, 1L, "Test Message", "email", false, LocalDateTime.now());

        when(notificationService.sendNotification(any(NotificationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.message").value("Test Message"));

        verify(notificationService, times(1)).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void testGetNotificationById() throws Exception {
        NotificationResponse response = new NotificationResponse(1L, 1L, "Test Message", "email", false, LocalDateTime.now());

        when(notificationService.getNotificationById(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L));

        verify(notificationService, times(1)).getNotificationById(1L);
    }

    @Test
    void testGetNotificationByIdNotFound() throws Exception {
        when(notificationService.getNotificationById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/notifications/999"))
                .andExpect(status().isNotFound());

        verify(notificationService, times(1)).getNotificationById(999L);
    }

    @Test
    void testGetNotificationsByUserId() throws Exception {
        List<NotificationResponse> responses = Arrays.asList(
                new NotificationResponse(1L, 1L, "Message 1", "email", false, LocalDateTime.now()),
                new NotificationResponse(2L, 1L, "Message 2", "sms", false, LocalDateTime.now())
        );

        when(notificationService.getNotificationsByUserId(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/notifications/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[1].userId").value(1L));

        verify(notificationService, times(1)).getNotificationsByUserId(1L);
    }

    @Test
    void testGetUnreadNotifications() throws Exception {
        List<NotificationResponse> responses = Arrays.asList(
                new NotificationResponse(1L, 1L, "Unread 1", "email", false, LocalDateTime.now()),
                new NotificationResponse(2L, 1L, "Unread 2", "email", false, LocalDateTime.now())
        );

        when(notificationService.getUnreadNotifications(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/notifications/user/1/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(notificationService, times(1)).getUnreadNotifications(1L);
    }

    @Test
    void testMarkAsRead() throws Exception {
        NotificationResponse response = new NotificationResponse(1L, 1L, "Test Message", "email", true, LocalDateTime.now());

        when(notificationService.markAsRead(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(put("/api/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));

        verify(notificationService, times(1)).markAsRead(1L);
    }

    @Test
    void testDeleteNotification() throws Exception {
        mockMvc.perform(delete("/api/notifications/1"))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).deleteNotification(1L);
    }

    @Test
    void testGetAllNotifications() throws Exception {
        List<NotificationResponse> responses = Arrays.asList(
                new NotificationResponse(1L, 1L, "Message 1", "email", false, LocalDateTime.now()),
                new NotificationResponse(2L, 2L, "Message 2", "sms", true, LocalDateTime.now())
        );

        when(notificationService.getAllNotifications()).thenReturn(responses);

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(notificationService, times(1)).getAllNotifications();
    }

    @Test
    void testGetNotificationsByUserIdEmpty() throws Exception {
        when(notificationService.getNotificationsByUserId(999L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/notifications/user/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(notificationService, times(1)).getNotificationsByUserId(999L);
    }

    @Test
    void testMarkAsReadNotFound() throws Exception {
        when(notificationService.markAsRead(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/notifications/999/read"))
                .andExpect(status().isNotFound());

        verify(notificationService, times(1)).markAsRead(999L);
    }

    @Test
    void testSendMultipleNotifications() throws Exception {
        NotificationRequest request1 = new NotificationRequest(1L, "Message 1", "email");
        NotificationRequest request2 = new NotificationRequest(1L, "Message 2", "sms");

        NotificationResponse response1 = new NotificationResponse(1L, 1L, "Message 1", "email", false, LocalDateTime.now());
        NotificationResponse response2 = new NotificationResponse(2L, 1L, "Message 2", "sms", false, LocalDateTime.now());

        when(notificationService.sendNotification(any(NotificationRequest.class)))
                .thenReturn(response1)
                .thenReturn(response2);

        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        verify(notificationService, times(2)).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void testGetUnreadNotificationsEmpty() throws Exception {
        when(notificationService.getUnreadNotifications(999L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/notifications/user/999/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(notificationService, times(1)).getUnreadNotifications(999L);
    }

    @Test
    void testDeleteNotificationNotFound() throws Exception {
        mockMvc.perform(delete("/api/notifications/999"))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).deleteNotification(999L);
    }

    @Test
    void testGetNotificationWithDifferentTypes() throws Exception {
        List<NotificationResponse> responses = Arrays.asList(
                new NotificationResponse(1L, 1L, "Email Message", "email", false, LocalDateTime.now()),
                new NotificationResponse(2L, 1L, "SMS Message", "sms", false, LocalDateTime.now()),
                new NotificationResponse(3L, 1L, "Push Message", "push", false, LocalDateTime.now())
        );

        when(notificationService.getNotificationsByUserId(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/notifications/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        verify(notificationService, times(1)).getNotificationsByUserId(1L);
    }
}
