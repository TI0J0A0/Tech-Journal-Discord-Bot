package com.techjournal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedItem {

    private String title;
    private String description;
    private String link;
    private String imageUrl;
    private ZonedDateTime publicationDate;
    private String source;
    private String author;

    public String getSafeDescription(int maxLength) {
        if (description == null || description.isBlank()) {
            return "Sem descrição disponível";
        }
        String cleaned = description
            .replaceAll("<[^>]*>", "")
            .replaceAll("&amp;", "&")
            .replaceAll("&lt;", "<")
            .replaceAll("&gt;", ">")
            .replaceAll("&quot;", "\"")
            .trim();

        if (cleaned.length() > maxLength) {
            return cleaned.substring(0, maxLength) + "...";
        }
        return cleaned;
    }
}
