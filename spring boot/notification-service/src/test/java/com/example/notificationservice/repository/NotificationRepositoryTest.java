package com.example.notificationservice.repository;

import com.example.notificationservice.entity.Notification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void testSaveNotification() {
        Notification notification = new Notification();
        notification.setUserId(1L);
        notification.setMessage("Test Notification");
        notification.setType("email");
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getUserId());
        assertEquals("Test Notification", saved.getMessage());
        assertFalse(saved.isRead());
    }

    @Test
    void testFindByUserId() {
        Notification notification = new Notification();
        notification.setUserId(2L);
        notification.setMessage("User Notification");
        notification.setType("sms");
        notification.setRead(false);

        notificationRepository.save(notification);

        List<Notification> found = notificationRepository.findByUserId(2L);

        assertFalse(found.isEmpty());
        assertEquals(2L, found.get(0).getUserId());
    }

    @Test
    void testFindByUserIdAndReadFalse() {
        Notification notification1 = new Notification();
        notification1.setUserId(3L);
        notification1.setMessage("Unread 1");
        notification1.setType("email");
        notification1.setRead(false);

        Notification notification2 = new Notification();
        notification2.setUserId(3L);
        notification2.setMessage("Read 1");
        notification2.setType("email");
        notification2.setRead(true);

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);

        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(3L);

        assertEquals(1, unread.size());
        assertFalse(unread.get(0).isRead());
    }

    @Test
    void testUpdateNotificationAsRead() {
        Notification notification = new Notification();
        notification.setUserId(4L);
        notification.setMessage("Update Test");
        notification.setType("email");
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        Long notificationId = saved.getId();

        saved.setRead(true);
        notificationRepository.save(saved);

        Optional<Notification> updated = notificationRepository.findById(notificationId);

        assertTrue(updated.isPresent());
        assertTrue(updated.get().isRead());
    }

    @Test
    void testDeleteNotification() {
        Notification notification = new Notification();
        notification.setUserId(5L);
        notification.setMessage("Delete Test");
        notification.setType("email");
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        Long notificationId = saved.getId();

        notificationRepository.deleteById(notificationId);

        Optional<Notification> deleted = notificationRepository.findById(notificationId);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testFindAll() {
        Notification notification1 = new Notification();
        notification1.setUserId(6L);
        notification1.setMessage("Notification 1");
        notification1.setType("email");

        Notification notification2 = new Notification();
        notification2.setUserId(7L);
        notification2.setMessage("Notification 2");
        notification2.setType("sms");

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);

        var all = notificationRepository.findAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void testNotificationWithDifferentTypes() {
        Notification emailNotif = new Notification();
        emailNotif.setUserId(8L);
        emailNotif.setMessage("Email Notification");
        emailNotif.setType("email");

        Notification smsNotif = new Notification();
        smsNotif.setUserId(8L);
        smsNotif.setMessage("SMS Notification");
        smsNotif.setType("sms");

        notificationRepository.save(emailNotif);
        notificationRepository.save(smsNotif);

        List<Notification> found = notificationRepository.findByUserId(8L);
        assertEquals(2, found.size());
    }
}
