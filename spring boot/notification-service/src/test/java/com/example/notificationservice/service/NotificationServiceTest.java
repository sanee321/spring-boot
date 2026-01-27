package com.example.notificationservice.service;

import com.example.notificationservice.dto.NotificationRequest;
import com.example.notificationservice.dto.NotificationResponse;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    public void testSendNotification() {
        NotificationRequest request = new NotificationRequest(1L, "ORDER", "Order Confirmed", "Your order has been confirmed");
        
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUserId(1L);
        notification.setType("ORDER");
        notification.setSubject("Order Confirmed");
        notification.setMessage("Your order has been confirmed");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponse response = notificationService.sendNotification(request);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("ORDER", response.getType());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void testGetNotificationsByUserId() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUserId(1L);
        notification.setType("ORDER");

        when(notificationRepository.findByUserId(1L)).thenReturn(Arrays.asList(notification));

        List<NotificationResponse> responses = notificationService.getNotificationsByUserId(1L);

        assertEquals(1, responses.size());
        verify(notificationRepository, times(1)).findByUserId(1L);
    }

    @Test
    public void testGetUnreadNotifications() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUserId(1L);
        notification.setRead(false);

        when(notificationRepository.findByUserIdAndReadFalse(1L)).thenReturn(Arrays.asList(notification));

        List<NotificationResponse> responses = notificationService.getUnreadNotifications(1L);

        assertEquals(1, responses.size());
        verify(notificationRepository, times(1)).findByUserIdAndReadFalse(1L);
    }

    @Test
    public void testMarkAsRead() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponse response = notificationService.markAsRead(1L);

        assertNotNull(response);
        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void testDeleteNotification() {
        when(notificationRepository.existsById(1L)).thenReturn(true);

        boolean result = notificationService.deleteNotification(1L);

        assertTrue(result);
        verify(notificationRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testGetNotificationById() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUserId(1L);
        notification.setType("ORDER");

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        Optional<NotificationResponse> response = notificationService.getNotificationById(1L);

        assertTrue(response.isPresent());
        assertEquals(1L, response.get().getUserId());
        verify(notificationRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetNotificationByIdNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<NotificationResponse> response = notificationService.getNotificationById(999L);

        assertFalse(response.isPresent());
        verify(notificationRepository, times(1)).findById(999L);
    }

    @Test
    public void testGetAllNotifications() {
        Notification notification1 = new Notification();
        notification1.setId(1L);
        notification1.setType("ORDER");

        Notification notification2 = new Notification();
        notification2.setId(2L);
        notification2.setType("PAYMENT");

        when(notificationRepository.findAll()).thenReturn(Arrays.asList(notification1, notification2));

        List<NotificationResponse> responses = notificationService.getAllNotifications();

        assertEquals(2, responses.size());
        verify(notificationRepository, times(1)).findAll();
    }

    @Test
    public void testGetNotificationsByUserIdEmpty() {
        when(notificationRepository.findByUserId(999L)).thenReturn(Arrays.asList());

        List<NotificationResponse> responses = notificationService.getNotificationsByUserId(999L);

        assertTrue(responses.isEmpty());
        verify(notificationRepository, times(1)).findByUserId(999L);
    }

    @Test
    public void testGetUnreadNotificationsEmpty() {
        when(notificationRepository.findByUserIdAndReadFalse(1L)).thenReturn(Arrays.asList());

        List<NotificationResponse> responses = notificationService.getUnreadNotifications(1L);

        assertTrue(responses.isEmpty());
        verify(notificationRepository, times(1)).findByUserIdAndReadFalse(1L);
    }

    @Test
    public void testMarkAsReadNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<NotificationResponse> response = notificationService.markAsRead(999L);

        assertFalse(response.isPresent());
        verify(notificationRepository, times(1)).findById(999L);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    public void testDeleteNotificationNotFound() {
        when(notificationRepository.existsById(999L)).thenReturn(false);

        boolean result = notificationService.deleteNotification(999L);

        assertFalse(result);
        verify(notificationRepository, never()).deleteById(any());
    }

    @Test
    public void testSendNotificationDifferentTypes() {
        NotificationRequest request = new NotificationRequest(2L, "PAYMENT", "Payment Received", "Payment processed successfully");
        
        Notification notification = new Notification();
        notification.setId(2L);
        notification.setUserId(2L);
        notification.setType("PAYMENT");

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponse response = notificationService.sendNotification(request);

        assertNotNull(response);
        assertEquals("PAYMENT", response.getType());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void testMarkMultipleAsRead() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setRead(false);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.markAsRead(1L);
        notificationService.markAsRead(1L);

        verify(notificationRepository, times(2)).findById(1L);
    }

    @Test
    public void testGetUnreadNotificationsWithMultiple() {
        Notification notification1 = new Notification();
        notification1.setId(1L);
        notification1.setRead(false);

        Notification notification2 = new Notification();
        notification2.setId(2L);
        notification2.setRead(false);

        when(notificationRepository.findByUserIdAndReadFalse(1L)).thenReturn(Arrays.asList(notification1, notification2));

        List<NotificationResponse> responses = notificationService.getUnreadNotifications(1L);

        assertEquals(2, responses.size());
        verify(notificationRepository, times(1)).findByUserIdAndReadFalse(1L);
    }

    @Test
    public void testSendNotificationWithEmail() {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setMessage("Email notification");
        request.setType("EMAIL");

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUserId(1L);
        notification.setMessage("Email notification");
        notification.setType("EMAIL");
        notification.setRead(false);

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponse response = notificationService.sendNotification(request);

        assertNotNull(response);
        assertEquals("EMAIL", response.getType());
        assertFalse(response.isRead());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void testSendNotificationWithSMS() {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(2L);
        request.setMessage("SMS notification");
        request.setType("SMS");

        Notification notification = new Notification();
        notification.setId(2L);
        notification.setUserId(2L);
        notification.setMessage("SMS notification");
        notification.setType("SMS");
        notification.setRead(false);

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponse response = notificationService.sendNotification(request);

        assertNotNull(response);
        assertEquals("SMS", response.getType());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void testGetNotificationsByUserIdWithResults() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUserId(1L);

        when(notificationRepository.findByUserId(1L)).thenReturn(Arrays.asList(notification));

        List<NotificationResponse> responses = notificationService.getNotificationsByUserId(1L);

        assertNotNull(responses);
        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        verify(notificationRepository, times(1)).findByUserId(1L);
    }

    @Test
    public void testMarkAsReadAlreadyRead() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setRead(true);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.markAsRead(1L);

        assertTrue(notification.isRead());
        verify(notificationRepository, times(1)).findById(1L);
    }

    @Test
    public void testDeleteNotificationSuccess() {
        when(notificationRepository.existsById(1L)).thenReturn(true);

        notificationService.deleteNotification(1L);

        verify(notificationRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testNotificationServiceIntegrationFlow() {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setMessage("Test message");
        request.setType("PUSH");

        Notification savedNotification = new Notification();
        savedNotification.setId(1L);
        savedNotification.setUserId(1L);
        savedNotification.setMessage("Test message");
        savedNotification.setType("PUSH");
        savedNotification.setRead(false);

        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(savedNotification));
        when(notificationRepository.findByUserId(1L)).thenReturn(Arrays.asList(savedNotification));

        NotificationResponse response = notificationService.sendNotification(request);
        assertNotNull(response);

        NotificationResponse retrieved = notificationService.getNotificationById(1L);
        assertNotNull(retrieved);

        List<NotificationResponse> userNotifications = notificationService.getNotificationsByUserId(1L);
        assertFalse(userNotifications.isEmpty());
    }
}
