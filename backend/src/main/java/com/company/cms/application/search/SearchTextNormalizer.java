package com.company.cms.application.search;

import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class SearchTextNormalizer {
    public String normalize(String input) {
        if (input == null) {
            return "";
        }
        return Normalizer.normalize(input, Normalizer.Form.NFKC)
            .toLowerCase(Locale.ROOT)
            .replaceAll("[\\p{Punct}&&[^가-힣a-z0-9\\s]]+", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    public boolean containsNormalized(String haystack, String needle) {
        String normalizedNeedle = normalize(needle);
        return normalizedNeedle.isBlank() || normalize(haystack).contains(normalizedNeedle);
    }
}
