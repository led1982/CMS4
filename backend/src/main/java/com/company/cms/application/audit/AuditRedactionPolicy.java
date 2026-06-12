package com.company.cms.application.audit;

import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AuditRedactionPolicy {
    private static final Set<String> SENSITIVE_KEYS = Set.of("body", "content", "password", "secret", "token", "filebytes");

    public Map<String, Object> redact(Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            return Map.of();
        }
        var redacted = new java.util.LinkedHashMap<String, Object>();
        details.forEach((key, value) -> redacted.put(String.valueOf(key), isSensitive(key) ? "[REDACTED]" : value == null ? "" : value));
        return Map.copyOf(redacted);
    }

    private boolean isSensitive(String key) {
        return key != null && SENSITIVE_KEYS.contains(key.trim().toLowerCase());
    }
}
