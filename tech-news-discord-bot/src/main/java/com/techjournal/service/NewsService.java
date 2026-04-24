package com.techjournal.service;

import com.techjournal.dto.FeedItem;
import com.techjournal.entity.SentNews;
import com.techjournal.repository.SentNewsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NewsService {

    private final RssFeedService rssFeedService;
    private final DiscordMessageService discordMessageService;
    private final SentNewsRepository sentNewsRepository;

    public NewsService(RssFeedService rssFeedService,
                       DiscordMessageService discordMessageService,
                       SentNewsRepository sentNewsRepository) {
        this.rssFeedService = rssFeedService;
        this.discordMessageService = discordMessageService;
        this.sentNewsRepository = sentNewsRepository;
    }

    public void fetchAndPublishNews() {
        log.info("Starting news fetch and publish cycle");

        try {
            List<FeedItem> feedItems = rssFeedService.fetchFeeds();
            log.info("Total items fetched from RSS feeds: {}", feedItems.size());

            int publishedCount = 0;
            for (FeedItem feedItem : feedItems) {
                if (isNewNews(feedItem)) {
                    publishNews(feedItem);
                    publishedCount++;
                } else {
                    log.debug("Skipping duplicate news: {}", feedItem.getTitle());
                }
            }

            log.info("Published {} new articles to Discord", publishedCount);
        } catch (Exception e) {
            log.error("Error in news fetch and publish cycle: {}", e.getMessage(), e);
        }
    }

    private boolean isNewNews(FeedItem feedItem) {
        return !sentNewsRepository.existsByLink(feedItem.getLink());
    }

    private void publishNews(FeedItem feedItem) {
        discordMessageService.sendNewsToDiscord(feedItem, () -> markAsSent(feedItem));
    }

    private void markAsSent(FeedItem feedItem) {
        SentNews sentNews = SentNews.builder()
            .link(feedItem.getLink())
            .title(feedItem.getTitle())
            .source(feedItem.getSource())
            .build();

        sentNewsRepository.save(sentNews);
        log.debug("Saved news to database: {}", feedItem.getTitle());
    }
}
