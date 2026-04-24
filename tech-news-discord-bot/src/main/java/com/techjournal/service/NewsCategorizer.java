package com.techjournal.service;

import com.techjournal.dto.FeedItem;
import org.springframework.stereotype.Component;

import java.awt.Color;

@Component
public class NewsCategorizer {

    public enum Category {
        SECURITY("🔒", "Segurança", new Color(237, 66, 69)),
        AI("🤖", "IA", new Color(155, 89, 182)),
        TECH("💻", "Tech", new Color(52, 152, 219)),
        BUSINESS("💼", "Negócios", new Color(46, 204, 113)),
        GENERAL("📰", "Geral", new Color(149, 165, 166));

        private final String emoji;
        private final String displayName;
        private final Color color;

        Category(String emoji, String displayName, Color color) {
            this.emoji = emoji;
            this.displayName = displayName;
            this.color = color;
        }

        public String getEmoji() { return emoji; }
        public String getDisplayName() { return displayName; }
        public Color getColor() { return color; }
    }

    public Category categorize(FeedItem item) {
        String haystack = buildHaystack(item);
        String source = item.getSource() != null ? item.getSource().toLowerCase() : "";

        if (isSecurity(source, haystack)) return Category.SECURITY;
        if (isAI(haystack)) return Category.AI;
        if (isBusiness(haystack)) return Category.BUSINESS;
        if (isTech(source)) return Category.TECH;

        return Category.GENERAL;
    }

    private String buildHaystack(FeedItem item) {
        StringBuilder sb = new StringBuilder();
        if (item.getTitle() != null) sb.append(item.getTitle().toLowerCase()).append(" ");
        if (item.getDescription() != null) sb.append(item.getDescription().toLowerCase());
        return sb.toString();
    }

    private boolean isSecurity(String source, String text) {
        if (source.contains("hacker news") || source.contains("security") ||
            source.contains("dark reading") || source.contains("krebs") ||
            source.contains("infosecurity")) {
            return true;
        }
        return containsAny(text, "vulnerability", "malware", "ransomware", "exploit",
            "cyberattack", "data breach", "zero-day", "phishing", "cve-", "hacker");
    }

    private boolean isAI(String text) {
        return containsAny(text, "openai", "chatgpt", " gpt", "anthropic", "claude",
            "gemini", "llm ", "machine learning", "artificial intelligence",
            "neural network", "deep learning");
    }

    private boolean isBusiness(String text) {
        return containsAny(text, "funding", "acquisition", "ipo", "merger",
            "startup raises", "valuation", "investors", "series a", "series b", "series c");
    }

    private boolean isTech(String source) {
        return source.contains("techcrunch") || source.contains("ars technica") ||
               source.contains("verge") || source.contains("wired");
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }
}
