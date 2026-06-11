package com.company.cms.domain.audit;

import com.company.cms.api.dto.CmsDtos;
import com.company.cms.api.dto.CmsDtos.PageMeta;
import com.company.cms.security.CmsSecurityContext.Personas;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final CopyOnWriteArrayList<AuditEventRecord> events = new CopyOnWriteArrayList<>();

    public AuditEventRecord record(String actorUserId, String action, String targetType, String targetId, String summary) {
        return record(actorUserId, action, targetType, targetId, summary, Map.of());
    }

    public AuditEventRecord record(String actorUserId, String action, String targetType, String targetId, String summary, Map<String, Object> details) {
        var event = new AuditEventRecord(
            UUID.randomUUID().toString(),
            actorUserId,
            action,
            targetType,
            targetId,
            summary,
            details == null ? Map.of() : Map.copyOf(details),
            Instant.now(),
            UUID.randomUUID().toString()
        );
        events.add(event);
        return event;
    }

    public CmsDtos.AuditEventListResponse search(String actorUserId, String targetType, String targetId, String action, int page, int size) {
        var filtered = events.stream()
            .filter(event -> actorUserId == null || event.actorUserId().equals(actorUserId))
            .filter(event -> targetType == null || event.targetType().equalsIgnoreCase(targetType))
            .filter(event -> targetId == null || event.targetId().equals(targetId))
            .filter(event -> action == null || event.action().equalsIgnoreCase(action))
            .sorted(Comparator.comparing(AuditEventRecord::occurredAt).reversed())
            .toList();
        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        var items = filtered.subList(from, to).stream().map(this::toDto).toList();
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);
        return new CmsDtos.AuditEventListResponse(items, new PageMeta(page, size, filtered.size(), totalPages));
    }

    public List<AuditEventRecord> all() {
        return events.stream()
            .sorted(Comparator.comparing(AuditEventRecord::occurredAt).reversed())
            .toList();
    }

    private CmsDtos.AuditEvent toDto(AuditEventRecord event) {
        return new CmsDtos.AuditEvent(
            event.id(),
            Personas.ref(event.actorUserId()),
            event.action(),
            event.targetType(),
            event.targetId(),
            event.summary(),
            event.details(),
            event.occurredAt(),
            event.requestId()
        );
    }

    public record AuditEventRecord(
        String id,
        String actorUserId,
        String action,
        String targetType,
        String targetId,
        String summary,
        Map<String, Object> details,
        Instant occurredAt,
        String requestId
    ) {
    }
}
