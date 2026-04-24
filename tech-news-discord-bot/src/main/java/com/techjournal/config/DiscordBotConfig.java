package com.techjournal.config;

import com.techjournal.properties.DiscordProperties;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DiscordBotConfig {

    @Bean
    public JDA jda(DiscordProperties discordProperties) throws InterruptedException {
        log.info("Initializing Discord Bot with JDA...");

        if (discordProperties.getToken() == null || discordProperties.getToken().isBlank()) {
            throw new IllegalArgumentException("Discord bot token not configured. Please set app.discord.token");
        }

        if (discordProperties.getChannelId() == null || discordProperties.getChannelId().isBlank()) {
            throw new IllegalArgumentException("Discord channel ID not configured. Please set app.discord.channel-id");
        }

        JDA jda = JDABuilder.createDefault(discordProperties.getToken())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
            .build()
            .awaitReady();

        log.info("Discord Bot successfully connected as: {}", jda.getSelfUser().getAsTag());

        return jda;
    }
}
