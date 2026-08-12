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
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.util.List;

@Slf4j
@Service
public class DiscordMessageService {

    private static final int FORUM_TITLE_MAX = 100;
    private static final int EMBED_TITLE_MAX = 256;
    private static final int EMBED_FIELD_VALUE_MAX = 1024;
    private static final int DESCRIPTION_MAX = 400;
    private static final int BUTTON_LABEL_MAX = 80;

    private static final String EMPTY_FIELD = "—";

    private final JDA jda;
    private final DiscordProperties discordProperties;
    private final NewsCategorizer categorizer;
    private final EmbedStyleResolver styleResolver;

    public DiscordMessageService(JDA jda,
                                 DiscordProperties discordProperties,
                                 NewsCategorizer categorizer,
                                 EmbedStyleResolver styleResolver) {
        this.jda = jda;
        this.discordProperties = discordProperties;
        this.categorizer = categorizer;
        this.styleResolver = styleResolver;
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
            Color color = styleResolver.resolveColor(feedItem);

            String postTitle = buildPostTitle(feedItem, category);
            MessageCreateData message = buildMessage(feedItem, category, color);
            List<ForumTagSnowflake> tags = resolveTags(forum, category);

            forum.createForumPost(postTitle, message)
                .setTags(tags)
                .queue(
                    success -> {
                        log.info("[{}] Post criado no fórum: {}",
                            category.getDisplayName(), feedItem.getTitle());
                        runSafely(onSuccess);
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

    private MessageCreateData buildMessage(FeedItem feedItem, Category category, Color color) {
        MessageCreateBuilder builder = new MessageCreateBuilder()
            .setEmbeds(buildEmbed(feedItem, category, color));

        ActionRow actionRow = buildActionRow(feedItem);
        if (actionRow != null) {
            builder.setComponents(actionRow);
        }

        return builder.build();
    }

    private MessageEmbed buildEmbed(FeedItem feedItem, Category category, Color color) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setColor(color);
        embed.setTitle(truncate(feedItem.getTitle(), EMBED_TITLE_MAX), safeLink(feedItem.getLink()));
        embed.setDescription(feedItem.getSafeDescription(DESCRIPTION_MAX));

        String imageUrl = feedItem.getImageUrl();
        if (isValidHttpUrl(imageUrl)) {
            embed.setImage(imageUrl);
        }

        embed.addField("📡 Fonte",     fieldValue(feedItem.getSource()), true);
        embed.addField("✍️ Autor",     fieldValue(feedItem.getAuthor()), true);
        embed.addField("🏷️ Categoria", category.getEmoji() + " " + category.getDisplayName(), true);

        if (feedItem.getPublicationDate() != null) {
            embed.setTimestamp(feedItem.getPublicationDate());
        }

        return embed.build();
    }

    private ActionRow buildActionRow(FeedItem feedItem) {
        String link = feedItem.getLink();
        if (!isValidHttpUrl(link)) {
            log.debug("Link inválido para botão — botão omitido: {}", link);
            return null;
        }
        Button readMore = Button.link(link, truncate("Ler matéria completa", BUTTON_LABEL_MAX))
            .withEmoji(Emoji.fromUnicode("🔗"));
        return ActionRow.of(readMore);
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

    private String fieldValue(String raw) {
        if (raw == null || raw.isBlank()) return EMPTY_FIELD;
        return truncate(raw, EMBED_FIELD_VALUE_MAX);
    }

    private String safeLink(String url) {
        return isValidHttpUrl(url) ? url : null;
    }

    private boolean isValidHttpUrl(String url) {
        return url != null && !url.isBlank()
            && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("Erro ao executar callback pós-envio: {}", e.getMessage(), e);
        }
    }
}
