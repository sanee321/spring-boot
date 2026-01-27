package com.example.notificationservice;

import com.example.notificationservice.dto.NotificationRequest;
import com.example.notificationservice.dto.NotificationResponse;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }

    @Test
    void testSendAndRetrieveNotification() {
        NotificationRequest request = new NotificationRequest(1L, "ORDER", "Order Confirmed", "Your order has been confirmed");
        NotificationResponse response = notificationService.sendNotification(request);

        assertNotNull(response);
        assertEquals("ORDER", response.getType());

        Optional<NotificationResponse> retrieved = notificationService.getNotificationById(response.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("ORDER", retrieved.get().getType());
    }

    @Test
    void testGetNotificationsByUserId() {
        NotificationRequest request1 = new NotificationRequest(1L, "ORDER", "Order Confirmed", "Message 1");
        NotificationRequest request2 = new NotificationRequest(1L, "PAYMENT", "Payment Received", "Message 2");

        notificationService.sendNotification(request1);
        notificationService.sendNotification(request2);

        List<NotificationResponse> notifications = notificationService.getNotificationsByUserId(1L);

        assertEquals(2, notifications.size());
    }

    @Test
    void testGetUnreadNotifications() {
        NotificationRequest request1 = new NotificationRequest(1L, "ORDER", "Order 1", "Message 1");
        NotificationRequest request2 = new NotificationRequest(1L, "ORDER", "Order 2", "Message 2");

        NotificationResponse response1 = notificationService.sendNotification(request1);
        NotificationResponse response2 = notificationService.sendNotification(request2);

        List<NotificationResponse> unread = notificationService.getUnreadNotifications(1L);

        assertEquals(2, unread.size());
    }

    @Test
    void testMarkAsReadFlow() {
        NotificationRequest request = new NotificationRequest(1L, "ORDER", "Order Confirmed", "Message");
        NotificationResponse created = notificationService.sendNotification(request);

        Optional<NotificationResponse> marked = notificationService.markAsRead(created.getId());

        assertTrue(marked.isPresent());
    }

    @Test
    void testDeleteNotificationFlow() {
        NotificationRequest request = new NotificationRequest(1L, "ORDER", "Order", "Message");
        NotificationResponse created = notificationService.sendNotification(request);

        boolean deleted = notificationService.deleteNotification(created.getId());

        assertTrue(deleted);
        Optional<NotificationResponse> notFound = notificationService.getNotificationById(created.getId());
        assertFalse(notFound.isPresent());
    }

    @Test
    void testMultipleNotificationsMultipleUsers() {
        notificationService.sendNotification(new NotificationRequest(1L, "ORDER", "Order 1", "Message 1"));
        notificationService.sendNotification(new NotificationRequest(2L, "ORDER", "Order 2", "Message 2"));
        notificationService.sendNotification(new NotificationRequest(3L, "PAYMENT", "Payment 3", "Message 3"));

        List<NotificationResponse> allNotifications = notificationService.getAllNotifications();

        assertTrue(allNotifications.size() >= 3);
    }

    @Test
    void testSendMultipleAndGetUnread() {
        NotificationRequest request1 = new NotificationRequest(1L, "ORDER", "Order 1", "Msg1");
        NotificationRequest request2 = new NotificationRequest(1L, "ORDER", "Order 2", "Msg2");
        NotificationRequest request3 = new NotificationRequest(1L, "ORDER", "Order 3", "Msg3");

        NotificationResponse r1 = notificationService.sendNotification(request1);
        NotificationResponse r2 = notificationService.sendNotification(request2);
        NotificationResponse r3 = notificationService.sendNotification(request3);

        notificationService.markAsRead(r1.getId());

        List<NotificationResponse> unread = notificationService.getUnreadNotifications(1L);

        assertEquals(2, unread.size());
    }

    @Test
    void testNotificationsByType() {
        NotificationRequest orderReq = new NotificationRequest(1L, "ORDER", "Order", "Msg");
        NotificationRequest paymentReq = new NotificationRequest(1L, "PAYMENT", "Payment", "Msg");

        notificationService.sendNotification(orderReq);
        notificationService.sendNotification(paymentReq);

        List<NotificationResponse> userNotifs = notificationService.getNotificationsByUserId(1L);

        assertEquals(2, userNotifs.size());
    }
}
