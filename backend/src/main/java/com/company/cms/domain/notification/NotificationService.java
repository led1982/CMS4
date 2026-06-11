package com.company.cms.domain.notification;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final CopyOnWriteArrayList<NotificationRecord> notifications = new CopyOnWriteArrayList<>();

    public NotificationRecord notify(String recipientUserId, String eventType, String contentItemId, String message) {
        var notification = new NotificationRecord(UUID.randomUUID().toString(), recipientUserId, eventType, contentItemId, message, null, Instant.now());
        notifications.add(notification);
        return notification;
    }

    public List<NotificationRecord> listForUser(String recipientUserId) {
        return notifications.stream()
            .filter(notification -> notification.recipientUserId().equals(recipientUserId))
            .toList();
    }

    public record NotificationRecord(
        String id,
        String recipientUserId,
        String eventType,
        String contentItemId,
        String message,
        Instant readAt,
        Instant createdAt
    ) {
    }
}
