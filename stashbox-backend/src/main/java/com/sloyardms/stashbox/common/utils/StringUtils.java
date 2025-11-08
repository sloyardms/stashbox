package com.sloyardms.stashbox.common.utils;

import com.github.slugify.Slugify;

import java.text.Normalizer;
import java.util.Locale;

public class StringUtils {

    private static final Slugify SLUGIFY = Slugify.builder()
            .transliterator(true)
            .locale(Locale.ENGLISH)
            .build();

    private StringUtils() {

    }

    /**
     * Converts text to a URL-friendly slug.
     * <p><strong>Example:</strong></p>
     * <pre>
     * slugify("Hello World")         → "hello-world"
     * slugify("Café-Bar!")           → "cafe-bar"
     * slugify("  Multiple   Spaces") → "multiple-spaces"
     * </pre>
     *
     * @param text the text to convert to a slug; may be null or blank
     * @return the slugified text, or null string if input is null or blank
     */
    public static String slugify(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return SLUGIFY.slugify(text);
    }

    /**
     * Performs text normalization including lowercase conversion, whitespace normalization, Unicode normalization
     * and accent removal.
     * <p><strong>Example:</strong></p>
     * <pre>
     * normalize("  HÉLLO   Wörld  ") → "hello world"
     * fullNormalize("Café-Bar")      → "cafe-bar"
     * fullNormalize("   ")           → null
     * fullNormalize(null)            → null
     * </pre>
     *
     * @param text the text to normalize; may be null or blank
     * @return the normalized text, or null string if input is null or blank
     */
    public static String normalize(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String unicodeNormalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        String withoutAccents = unicodeNormalized.replaceAll("\\p{M}", "");
        String lowercase = withoutAccents.toLowerCase(Locale.ROOT);
        return lowercase.trim().replaceAll("\\s+", " ");
    }
}
