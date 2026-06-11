package com.company.cms.performance;

import com.company.cms.application.search.SearchTextNormalizer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CmsPerformanceSmokeTest {
    @Test
    void koreanAndEnglishNormalizationIsStableForLargeFixtureTerms() {
        var normalizer = new SearchTextNormalizer();
        String text = "보안 정책 Security Policy " + "password reset ".repeat(50_000);
        long start = System.nanoTime();
        assertThat(normalizer.containsNormalized(text, "보안 Security")).isTrue();
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertThat(elapsedMillis).isLessThan(2_000);
    }
}
