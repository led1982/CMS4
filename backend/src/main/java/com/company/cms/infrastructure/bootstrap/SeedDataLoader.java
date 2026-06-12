package com.company.cms.infrastructure.bootstrap;

import org.springframework.stereotype.Component;

@Component
public class SeedDataLoader {
    /*
     * The MVP uses in-memory repositories whose @PostConstruct methods seed personas,
     * taxonomy, audiences, and sample content. This class marks the bootstrap boundary
     * for replacing that seed path with database-backed loading.
     */
}
