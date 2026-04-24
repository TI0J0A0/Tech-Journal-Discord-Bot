package com.techjournal.service;

import com.techjournal.dto.FeedItem;
import com.techjournal.properties.FeedProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RssFeedService {

    private static final String USER_AGENT =
        "Mozilla/5.0 (compatible; TechJournalBot/1.0; +https://github.com/)";
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 15_000;

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
        connection.setRequestProperty("Accept", "application/rss+xml, application/atom+xml, application/xml;q=0.9, */*;q=0.8");
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

    private String extractImageUrl(SyndEntry entry) {
        if (entry.getEnclosures() != null && !entry.getEnclosures().isEmpty()) {
            String type = entry.getEnclosures().get(0).getType();
            if (type != null && type.startsWith("image/")) {
                return entry.getEnclosures().get(0).getUrl();
            }
        }

        if (entry.getContents() != null && !entry.getContents().isEmpty()) {
            String content = entry.getContents().get(0).getValue();
            if (content != null) {
                return extractImageUrlFromHtml(content);
            }
        }

        return null;
    }

    private String extractImageUrlFromHtml(String html) {
        if (html == null) return null;

        int imgIndex = html.indexOf("<img");
        if (imgIndex != -1) {
            int srcIndex = html.indexOf("src=\"", imgIndex);
            if (srcIndex != -1) {
                int endIndex = html.indexOf("\"", srcIndex + 5);
                if (endIndex != -1) {
                    return html.substring(srcIndex + 5, endIndex);
                }
            }
        }

        return null;
    }
}
