# 🎨 Customização & Boas Práticas

Guia avançado para personalizar seu bot de acordo com suas necessidades.

---

## 1. Personalizar Feeds RSS

### Adicionar Novos Feeds

Edite `src/main/resources/application.properties`:

```properties
# Adicione quantos quiser
app.feeds.urls[0]=https://feeds.techcrunch.com/techcrunch/
app.feeds.urls[1]=https://feeds.arstechnica.com/arstechnica/index
app.feeds.urls[2]=https://thehackernews.com/feeds/posts/default?alt=rss
app.feeds.urls[3]=https://feeds.securityweek.com/securityweek
app.feeds.urls[4]=https://www.infosecurity-magazine.com/feed/rss/
app.feeds.urls[5]=https://www.darkreading.com/feeds/darkreadingfull.xml
app.feeds.urls[6]=https://feeds.reuters.com/reuters/technologyNews
app.feeds.urls[7]=https://feeds.bloomberg.com/technology/
```

### Validar URL de Feed

```bash
# Testar se a URL de feed está acessível
curl -I https://feeds.techcrunch.com/techcrunch/

# Ou baixar e inspecionar o XML
curl https://feeds.techcrunch.com/techcrunch/ | head -20
```

### Remover Feeds

Simplesmente remova ou comente a linha do feed que não quer:

```properties
# app.feeds.urls[5]=https://unwanted-feed.com/rss
```

### Feeds Recomendados por Categoria

**Tecnologia Geral:**
- TechCrunch: `https://feeds.techcrunch.com/techcrunch/`
- ArsTechnica: `https://feeds.arstechnica.com/arstechnica/index`
- The Verge: `https://www.theverge.com/rss/index.xml`

**Cibersegurança:**
- The Hacker News: `https://thehackernews.com/feeds/posts/default?alt=rss`
- SecurityWeek: `https://feeds.securityweek.com/securityweek`
- Dark Reading: `https://www.darkreading.com/feeds/darkreadingfull.xml`
- Krebs on Security: `https://feeds.krebsonsecurity.com/krebsonsecurity`

**Cloud & DevOps:**
- CloudFlare Blog: `https://blog.cloudflare.com/feed/`
- AWS Blog: `https://aws.amazon.com/blogs/aws/feed/`
- Kubernetes Blog: `https://kubernetes.io/feed.xml`

**IA & Machine Learning:**
- OpenAI Blog: `https://openai.com/blog/feed.rss`
- DeepMind Blog: `https://deepmind.com/blog?format=rss`

---

## 2. Personalizar Agendamento

### Usar CRON em vez de Fixed Delay

Edite `src/main/java/com/techjournal/scheduler/FeedScheduledTask.java`:

```java
// ANTES (Fixed delay)
@Scheduled(fixedDelayString = "${app.scheduler.fixed-delay:3600000}",
           initialDelayString = "${app.scheduler.initial-delay:5000}")
public void fetchAndPublishNews() {
    // ...
}

// DEPOIS (CRON - meia-noite todos os dias)
@Scheduled(cron = "0 0 * * * ?")
public void fetchAndPublishNews() {
    newsService.fetchAndPublishNews();
}
```

### Exemplos de CRON

```properties
# Meia-noite todos os dias
0 0 * * * ?

# 9h, 14h e 19h todos os dias
0 9,14,19 * * * ?

# A cada 6 horas
0 0 */6 * * * ?

# A cada 30 minutos
0 */30 * * * * ?

# Segunda a Sexta às 9h
0 9 * * 1-5 ?

# Fim de semana à noite
0 20 * * 6,0 ?

# Primeira segunda-feira de cada mês
0 9 ? * 1#1 ?
```

### Múltiplas Tarefas Agendadas

```java
@Component
@Slf4j
public class AdvancedScheduler {

    @Scheduled(cron = "0 9 * * * ?")  // 9h - Business hours
    public void businessHoursFetch() {
        // Busca mais frequente durante o dia
    }

    @Scheduled(cron = "0 0 * * * ?")  // Meia-noite
    public void nightlyDeepScan() {
        // Busca mais profunda à noite
    }

    @Scheduled(cron = "0 0 0 1 * ?")  // Primeiro dia do mês
    public void monthlyCleanup() {
        // Limpeza de registros antigos
    }
}
```

---

## 3. Personalizar Formatação de Mensagens

### Mudar Cores do Embed

Edite `src/main/java/com/techjournal/service/DiscordMessageService.java`:

```java
// Trocar cores
embed.setColor(Color.CYAN);  // Padrão

// Outras opções
embed.setColor(Color.RED);       // Vermelho
embed.setColor(Color.GREEN);     // Verde
embed.setColor(Color.BLUE);      // Azul
embed.setColor(Color.YELLOW);    // Amarelo
embed.setColor(Color.MAGENTA);   // Magenta
embed.setColor(new Color(255, 20, 147));  // Custom RGB (Deep Pink)
```

### Adicionar Campos Customizados

```java
private MessageEmbed buildNewsEmbed(FeedItem feedItem) {
    EmbedBuilder embed = new EmbedBuilder();
    
    // ... campos existentes ...
    
    // Adicionar campo customizado
    embed.addField("Categoria", "Segurança", false);
    embed.addField("Prioridade", "🔴 Alta", true);
    embed.addField("Status", "✅ Verificado", true);
    
    return embed.build();
}
```

### Adicionar Thumbnails

```java
embed.setThumbnail("https://seu-logo-url.com/logo.png");
```

### Modificar Descrição e Footer

```java
// Adicionar mais contexto
embed.appendDescription("\n\n_Ultima atualização: " + 
    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + "_");

// Footer customizado
embed.setFooter("🚀 Tech Bot v1.0 | " + feedItem.getSource(), 
                "https://url-do-seu-logo.png");
```

---

## 4. Filtrar Notícias

### Exemplo: Apenas Security News

Edite `NewsService.java`:

```java
private void publishNews(FeedItem feedItem) {
    // Filtro: apenas artigos com palavras-chave de segurança
    String keywords[] = {"hack", "breach", "vulnerability", "malware", "ransomware", "exploit"};
    
    boolean containsKeyword = false;
    for (String keyword : keywords) {
        if (feedItem.getTitle().toLowerCase().contains(keyword) ||
            feedItem.getDescription().toLowerCase().contains(keyword)) {
            containsKeyword = true;
            break;
        }
    }
    
    if (!containsKeyword && feedItem.getSource().equals("TechCrunch")) {
        log.debug("Skipping non-security article: {}", feedItem.getTitle());
        return;  // Pular notícia
    }
    
    // ... resto do código ...
}
```

### Exemplo: Filtrar por Fonte

```java
// Apenas artigos do The Hacker News
if (!feedItem.getSource().equals("The Hacker News")) {
    return;
}
```

### Exemplo: Filtrar por Data

```java
// Apenas artigos dos últimos 6 horas
LocalDateTime sixHoursAgo = LocalDateTime.now().minusHours(6);
if (feedItem.getPublicationDate().isBefore(sixHoursAgo)) {
    return;
}
```

---

## 5. Aumentar Limites de Caracteres

### Descrição Mais Longa

Edite `DiscordMessageService.java`:

```java
// Aumentar de 300 para 1000 caracteres
embed.setDescription(feedItem.getSafeDescription(1000));
```

### Título Mais Longo

```java
// Aumentar de 256 para 350 (Discord max é 256, essa função trunca)
embed.setTitle(truncate(feedItem.getTitle(), 350), feedItem.getLink());
```

---

## 6. Persistência Avançada

### Limpeza de Registros Antigos

Crie um novo método em `NewsService`:

```java
public void cleanupOldNews(int daysOld) {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
    List<SentNews> oldNews = sentNewsRepository.findBySentAtBefore(cutoffDate);
    
    log.info("Deleting {} old news records", oldNews.size());
    sentNewsRepository.deleteAll(oldNews);
}
```

Adicione ao repository:

```java
List<SentNews> findBySentAtBefore(LocalDateTime date);
```

Agende a limpeza:

```java
@Scheduled(cron = "0 0 3 * * ?")  // 3h da manhã todos os dias
public void dailyCleanup() {
    newsService.cleanupOldNews(90);  // Remove notícias com mais de 90 dias
}
```

### Estatísticas de Publicação

```java
@Service
public class NewsStatsService {
    
    @Autowired
    private SentNewsRepository repository;
    
    public long getPublishedCount() {
        return repository.count();
    }
    
    public long getPublishedToday() {
        LocalDateTime startOfDay = LocalDateTime.now()
            .withHour(0).withMinute(0).withSecond(0);
        return repository.countBySentAtAfter(startOfDay);
    }
}
```

---

## 7. Notificações Customizadas

### Mencionar @everyone para News Importantes

```java
private void publishNews(FeedItem feedItem) {
    if (isHighPriority(feedItem)) {
        channel.sendMessage("@everyone " + feedItem.getTitle()).queue();
    }
    
    // Enviar embed normalmente
    discordMessageService.sendNewsToDiscord(feedItem);
}

private boolean isHighPriority(FeedItem feedItem) {
    return feedItem.getTitle().toLowerCase().contains("zero-day") ||
           feedItem.getTitle().toLowerCase().contains("critical");
}
```

### Usar Threads no Discord

```java
channel.sendMessage("**Nova categoria de notícias**")
    .queue(message -> {
        message.createThreadChannel("Tech Discussion").queue(thread -> {
            thread.sendMessageEmbeds(embed).queue();
        });
    });
```

---

## 8. Logging Avançado

### Aumentar Verbosidade

Edite `application.properties`:

```properties
logging.level.com.techjournal=DEBUG
logging.level.com.techjournal.service=TRACE
```

### Salvar Logs em Arquivo

```properties
logging.file.name=logs/bot.log
logging.file.max-size=10MB
logging.file.max-history=30
```

### Formato Customizado

```properties
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

---

## 9. Métricas e Monitoramento

### Adicionar Contador de Publicações

```java
@Service
@Slf4j
public class MetricsService {
    
    private AtomicInteger publishedCount = new AtomicInteger(0);
    private AtomicInteger skippedCount = new AtomicInteger(0);
    
    public void incrementPublished() {
        publishedCount.incrementAndGet();
    }
    
    public void incrementSkipped() {
        skippedCount.incrementAndGet();
    }
    
    public Map<String, Integer> getMetrics() {
        return Map.of(
            "published", publishedCount.get(),
            "skipped", skippedCount.get()
        );
    }
}
```

### Endpoint REST para Status

```java
@RestController
@RequestMapping("/api")
public class BotStatusController {
    
    @Autowired
    private MetricsService metrics;
    
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return Map.of(
            "status", "running",
            "metrics", metrics.getMetrics(),
            "timestamp", LocalDateTime.now()
        );
    }
}
```

---

## 10. Boas Práticas de Segurança

### Nunca Commit Tokens

```bash
# Adicionar ao .gitignore
echo "application-local.properties" >> .gitignore
echo ".env" >> .gitignore
```

### Validar URLs de Feed

```java
private boolean isValidFeedUrl(String url) {
    try {
        URL u = new URL(url);
        return u.getProtocol().equals("https") || u.getProtocol().equals("http");
    } catch (MalformedURLException e) {
        return false;
    }
}
```

### Rate Limiting

```java
private static final Queue<LocalDateTime> requestTimes = new ConcurrentLinkedQueue<>();
private static final int MAX_REQUESTS = 100;
private static final Duration WINDOW = Duration.ofMinutes(1);

public boolean isRateLimited() {
    LocalDateTime now = LocalDateTime.now();
    requestTimes.removeIf(time -> 
        time.isBefore(now.minus(WINDOW))
    );
    
    if (requestTimes.size() >= MAX_REQUESTS) {
        return true;
    }
    
    requestTimes.offer(now);
    return false;
}
```

---

## 11. Integração com Outras Plataformas

### Enviar para Slack também

```java
@Service
public class SlackNotificationService {
    
    public void sendToSlack(FeedItem feedItem) {
        // Implementar integração com Slack Webhook
    }
}
```

### Enviar para Email

```java
@Service
public class EmailNotificationService {
    
    @Autowired
    private JavaMailSender emailSender;
    
    public void sendEmailDigest(List<FeedItem> items) {
        // Implementar digest diário por email
    }
}
```

---

## 📚 Exemplo Completo: Bot Personalizado

Aqui está uma customização completa de exemplo:

```java
// FeedScheduledTask customizado
@Scheduled(cron = "0 9,14,19 * * *")  // 9h, 14h, 19h
public void businessHoursFetch() {
    newsService.fetchAndPublishNews();
}

// Com filtro de segurança
private boolean isSecurityRelevant(FeedItem item) {
    String securityKeywords = "hack|breach|vulnerability|malware|attack|threat";
    Pattern pattern = Pattern.compile(securityKeywords, Pattern.CASE_INSENSITIVE);
    
    return pattern.matcher(item.getTitle()).find() ||
           pattern.matcher(item.getDescription()).find();
}
```

---

**Divirta-se customizando! Explore as possibilidades! 🎉**
