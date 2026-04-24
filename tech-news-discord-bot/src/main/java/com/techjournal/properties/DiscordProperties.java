package com.techjournal.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.discord")
@Data
public class DiscordProperties {

    private String token;
    private String channelId;

}
