package com.techjournal.service;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.techjournal.dto.FeedItem;
import com.techjournal.properties.FeedProperties;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.Element;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class RssFeedService {

    private static final String USER_AGENT =
        "Mozilla/5.0 (compatible; TechJournalBot/1.0; +https://github.com/)";
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 15_000;

    private static final Pattern IMG_SRC_PATTERN = Pattern.compile(
        "<img[^>]+src\\s*=\\s*[\"']([^\"']+)[\"']",
        Pattern.CASE_INSENSITIVE
    );

    private final FeedProperties feedProperties;
    private final SyndFeedInput syndFeedInput;

    public RssFeedService(FeedProperties feedProperties) {
        this.feedProperties = feedProperties;
        this.syndFeedInput = new SyndFeedInput();
    }

    public List<FeedItem> fetchFeeds() {
        List<FeedItem> allItems = new ArrayList<>();

        for (String feedUrl : feedProperties.getUrls()) {
            try {
                List<FeedItem> items = fetchSingleFeed(feedUrl);
                log.info("Fetched {} items from feed: {}", items.size(), feedUrl);
                allItems.addAll(items);
            } catch (Exception e) {
                log.error("Error fetching RSS feed from {}: {}", feedUrl, e.getMessage(), e);
            }
        }

        return allItems;
    }

    private List<FeedItem> fetchSingleFeed(String feedUrl) throws Exception {
        List<FeedItem> items = new ArrayList<>();

        HttpURLConnection connection = (HttpURLConnection) new URL(feedUrl).openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Accept",
            "application/rss+xml, application/atom+xml, application/xml;q=0.9, */*;q=0.8");
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setInstanceFollowRedirects(true);

        try (XmlReader xmlReader = new XmlReader(connection)) {
            SyndFeed feed = syndFeedInput.build(xmlReader);

            for (SyndEntry entry : feed.getEntries()) {
                FeedItem item = parseSyndEntry(entry, feed.getTitle());
                items.add(item);
            }
        } finally {
            connection.disconnect();
        }

        return items;
    }

    private FeedItem parseSyndEntry(SyndEntry entry, String sourceName) {
        return FeedItem.builder()
            .title(entry.getTitle())
            .description(entry.getDescription() != null ? entry.getDescription().getValue() : null)
            .link(entry.getLink())
            .publicationDate(entry.getPublishedDate() != null
                ? entry.getPublishedDate().toInstant().atZone(java.time.ZoneId.systemDefault())
                : null)
            .author(entry.getAuthor())
            .source(sourceName)
            .imageUrl(extractImageUrl(entry))
            .build();
    }

    /**
     * Busca ativamente uma imagem nos seguintes locais, em ordem:
     * 1. <enclosure type="image/*" url="..."/>
     * 2. <media:content url="..."/> e <media:thumbnail url="..."/> (namespace Media RSS)
     * 3. Primeira tag <img src="..."/> no conteúdo (<content:encoded>)
     * 4. Primeira tag <img src="..."/> na descrição
     */
    private String extractImageUrl(SyndEntry entry) {
        String url = extractFromEnclosures(entry);
        if (isValidImageUrl(url)) return url;

        url = extractFromMediaNamespace(entry);
        if (isValidImageUrl(url)) return url;

        url = extractFromContents(entry);
        if (isValidImageUrl(url)) return url;

        url = extractFromDescription(entry);
        if (isValidImageUrl(url)) return url;

        return null;
    }

    private String extractFromEnclosures(SyndEntry entry) {
        if (entry.getEnclosures() == null) return null;
        for (Object o : entry.getEnclosures()) {
            if (!(o instanceof SyndEnclosure enc)) continue;
            String type = enc.getType();
            String url = enc.getUrl();
            if (url == null || url.isBlank()) continue;
            if (type == null || type.startsWith("image/")) return url;
        }
        return null;
    }

    private String extractFromMediaNamespace(SyndEntry entry) {
        if (entry.getForeignMarkup() == null) return null;

        for (Element element : entry.getForeignMarkup()) {
            String prefix = element.getNamespacePrefix();
            String name = element.getName();

            if (!"media".equalsIgnoreCase(prefix)) continue;
            if (!("content".equalsIgnoreCase(name) || "thumbnail".equalsIgnoreCase(name))) continue;

            String url = element.getAttributeValue("url");
            if (url != null && !url.isBlank()) return url;
        }
        return null;
    }

    private String extractFromContents(SyndEntry entry) {
        if (entry.getContents() == null) return null;
        for (SyndContent content : entry.getContents()) {
            String url = firstImgSrc(content.getValue());
            if (url != null) return url;
        }
        return null;
    }

    private String extractFromDescription(SyndEntry entry) {
        if (entry.getDescription() == null) return null;
        return firstImgSrc(entry.getDescription().getValue());
    }

    private String firstImgSrc(String html) {
        if (html == null || html.isBlank()) return null;
        Matcher matcher = IMG_SRC_PATTERN.matcher(html);
        return matcher.find() ? matcher.group(1) : null;
    }

    private boolean isValidImageUrl(String url) {
        return url != null && !url.isBlank()
            && (url.startsWith("http://") || url.startsWith("https://"));
    }
}
