package com.techjournal.scheduler;

import com.techjournal.service.NewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeedScheduledTask {

    private final NewsService newsService;

    public FeedScheduledTask(NewsService newsService) {
        this.newsService = newsService;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.fixed-delay:3600000}",
               initialDelayString = "${app.scheduler.initial-delay:5000}")
    public void fetchAndPublishNews() {
        log.info("============ News Fetch Task Started ============");
        newsService.fetchAndPublishNews();
        log.info("============ News Fetch Task Completed ============");
    }

    // Alternativa: usar cron para horários específicos
    // @Scheduled(cron = "0 */4 * * * ?") // A cada 4 horas
    // public void fetchAndPublishNewsCron() {
    //     newsService.fetchAndPublishNews();
    // }
}
