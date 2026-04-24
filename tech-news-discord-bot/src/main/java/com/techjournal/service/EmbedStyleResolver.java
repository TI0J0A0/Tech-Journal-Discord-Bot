package com.techjournal.service;

import com.techjournal.dto.FeedItem;
import com.techjournal.service.NewsCategorizer.Category;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class EmbedStyleResolver {

    private static final Color NEUTRAL_DARK_GRAY = new Color(47, 49, 54);

    /** Match por substring no nome da fonte (case-insensitive). Ordem importa — primeiro match vence. */
    private static final Map<String, Color> SOURCE_COLORS = new LinkedHashMap<>();

    static {
        SOURCE_COLORS.put("ars technica",     new Color(30, 136, 229));   // Azul
        SOURCE_COLORS.put("the hacker news",  new Color(229, 57, 53));    // Vermelho
        SOURCE_COLORS.put("techcrunch",       new Color(0, 200, 83));     // Verde
        SOURCE_COLORS.put("the verge",        new Color(250, 67, 30));    // Laranja/Vermelho
        SOURCE_COLORS.put("wired",            new Color(0, 0, 0));        // Preto
        SOURCE_COLORS.put("securityweek",     new Color(0, 61, 122));     // Azul escuro
        SOURCE_COLORS.put("dark reading",     new Color(139, 0, 0));      // Vermelho escuro
        SOURCE_COLORS.put("krebs",            new Color(230, 81, 0));     // Laranja
        SOURCE_COLORS.put("bleeping computer", new Color(25, 118, 210));  // Azul
    }

    private final NewsCategorizer categorizer;

    public EmbedStyleResolver(NewsCategorizer categorizer) {
        this.categorizer = categorizer;
    }

    public Color resolveColor(FeedItem item) {
        Color fromSource = matchSourceColor(item.getSource());
        if (fromSource != null) return fromSource;

        Category category = categorizer.categorize(item);
        if (category != Category.GENERAL) return category.getColor();

        return NEUTRAL_DARK_GRAY;
    }

    private Color matchSourceColor(String source) {
        if (source == null || source.isBlank()) return null;
        String normalized = source.toLowerCase();
        for (Map.Entry<String, Color> entry : SOURCE_COLORS.entrySet()) {
            if (normalized.contains(entry.getKey())) return entry.getValue();
        }
        return null;
    }
}
