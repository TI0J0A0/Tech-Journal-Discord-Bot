package com.techjournal.service;

import com.techjournal.dto.FeedItem;
import com.techjournal.properties.DiscordProperties;
import com.techjournal.service.NewsCategorizer.Category;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class DiscordMessageService {

    private static final int FORUM_TITLE_MAX = 100;
    private static final int EMBED_TITLE_MAX = 256;
    private static final int DESCRIPTION_MAX = 400;

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", new Locale("pt", "BR"));

    private final JDA jda;
    private final DiscordProperties discordProperties;
    private final NewsCategorizer categorizer;

    public DiscordMessageService(JDA jda,
                                 DiscordProperties discordProperties,
                                 NewsCategorizer categorizer) {
        this.jda = jda;
        this.discordProperties = discordProperties;
        this.categorizer = categorizer;
    }

    public void sendNewsToDiscord(FeedItem feedItem, Runnable onSuccess) {
        try {
            ForumChannel forum = jda.getForumChannelById(discordProperties.getChannelId());

            if (forum == null) {
                log.error("Forum channel with ID {} not found. Verifique se o ID é de um " +
                    "canal de FÓRUM (não de texto).", discordProperties.getChannelId());
                return;
            }

            Category category = categorizer.categorize(feedItem);
            String postTitle = buildPostTitle(feedItem, category);
            MessageCreateData messageData = buildPostContent(feedItem, category);
            List<ForumTagSnowflake> tags = resolveTags(forum, category);

            forum.createForumPost(postTitle, messageData)
                .setTags(tags)
                .queue(
                    success -> {
                        log.info("[{}] Post criado no fórum: {}",
                            category.getDisplayName(), feedItem.getTitle());
                        try {
                            onSuccess.run();
                        } catch (Exception e) {
                            log.error("Erro ao executar callback pós-envio: {}", e.getMessage(), e);
                        }
                    },
                    error -> log.error("Falha ao criar post no fórum para '{}': {}",
                        feedItem.getTitle(), error.getMessage())
                );
        } catch (Exception e) {
            log.error("Erro ao enviar notícia para o Discord: {}", e.getMessage(), e);
        }
    }

    private String buildPostTitle(FeedItem feedItem, Category category) {
        String prefix = category.getEmoji() + " ";
        int available = FORUM_TITLE_MAX - prefix.length();
        return prefix + truncate(feedItem.getTitle(), available);
    }

    private MessageCreateData buildPostContent(FeedItem feedItem, Category category) {
        String intro = buildIntroLine(feedItem, category);
        MessageEmbed embed = buildNewsEmbed(feedItem, category);
        return new MessageCreateBuilder()
            .setContent(intro)
            .setEmbeds(embed)
            .build();
    }

    private String buildIntroLine(FeedItem feedItem, Category category) {
        StringBuilder sb = new StringBuilder();
        sb.append("> ").append(category.getEmoji()).append(" **").append(category.getDisplayName()).append("**");
        sb.append(" • 📡 `").append(feedItem.getSource()).append("`");

        if (feedItem.getPublicationDate() != null) {
            sb.append(" • 🕐 ").append(feedItem.getPublicationDate().format(DATE_FORMATTER));
        }
        return sb.toString();
    }

    private MessageEmbed buildNewsEmbed(FeedItem feedItem, Category category) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle(truncate(feedItem.getTitle(), EMBED_TITLE_MAX), feedItem.getLink());
        embed.setDescription(feedItem.getSafeDescription(DESCRIPTION_MAX));
        embed.setColor(category.getColor());

        if (feedItem.getImageUrl() != null && !feedItem.getImageUrl().isBlank()) {
            embed.setImage(feedItem.getImageUrl());
        }

        embed.addField("📡 Fonte", feedItem.getSource(), true);

        if (feedItem.getAuthor() != null && !feedItem.getAuthor().isBlank()) {
            embed.addField("✍️ Autor", truncate(feedItem.getAuthor(), 256), true);
        }

        embed.addField("🏷️ Categoria",
            category.getEmoji() + " " + category.getDisplayName(), true);

        if (feedItem.getPublicationDate() != null) {
            embed.setTimestamp(feedItem.getPublicationDate());
        }

        embed.setFooter("Tech Journal • Clique no título para ler a matéria completa");

        return embed.build();
    }

    private List<ForumTagSnowflake> resolveTags(ForumChannel forum, Category category) {
        String target = category.getDisplayName().toLowerCase();
        return forum.getAvailableTags().stream()
            .filter(tag -> tag.getName().toLowerCase().contains(target))
            .map(ForumTag::getIdLong)
            .map(ForumTagSnowflake::fromId)
            .limit(5)
            .toList();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
