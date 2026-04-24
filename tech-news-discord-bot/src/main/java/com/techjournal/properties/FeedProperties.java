package com.techjournal.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.feeds")
@Data
public class FeedProperties {

    private List<String> urls = new ArrayList<>();

}
