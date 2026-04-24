# 🏗️ Arquitetura & Design do Projeto

Documentação técnica detalhada sobre a arquitetura, padrões de design e decisões arquiteturais.

---

## 📐 Visão Geral da Arquitetura

```
┌─────────────────────────────────────────────────────────────────┐
│                    Tech News Discord Bot                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           Spring Boot Application Layer                  │  │
│  │  • Application Main Class                               │  │
│  │  • Component Scanning & Auto-wiring                     │  │
│  │  • Configuration Properties                             │  │
│  └──────────────────────────────────────────────────────────┘  │
│                            ▲                                    │
│                            │                                    │
│  ┌─────────────────────────┴──────────────────────────────────┐ │
│  │                  Presentation Layer                        │ │
│  │  ┌─────────────────────┐       ┌──────────────────────┐   │ │
│  │  │ FeedScheduledTask   │       │ BotStatusController │   │ │
│  │  │ (Cron/Fixed Delay)  │       │ (REST API)           │   │ │
│  │  └──────────┬──────────┘       └──────────────────────┘   │ │
│  └─────────────┼─────────────────────────────────────────────┘ │
│                │                                               │
│  ┌─────────────┴─────────────────────────────────────────────┐ │
│  │              Business Logic Layer                         │ │
│  │  ┌──────────────────┐  ┌──────────────────┐               │ │
│  │  │  NewsService     │  │ MetricsService   │               │ │
│  │  │ • Orchestration  │  │ • Statistics     │               │ │
│  │  │ • Deduplication  │  │ • Tracking       │               │ │
│  │  └────────┬─────────┘  └──────────────────┘               │ │
│  └───────────┼────────────────────────────────────────────────┘ │
│              │                                                 │
│  ┌───────────┴───────────────────────────────────────────────┐ │
│  │        Service Integration Layer                          │ │
│  │  ┌──────────────────┐   ┌──────────────────────────────┐ │ │
│  │  │ RssFeedService   │   │ DiscordMessageService        │ │ │
│  │  │ • RSS Parsing    │   │ • Embed Formatting           │ │ │
│  │  │ • Image Extract  │   │ • Message Sending (JDA)      │ │ │
│  │  │ • Validation     │   │ • Error Handling             │ │ │
│  │  └─────────────────┘   └──────────────────────────────┘ │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           External Integration Layer                     │  │
│  │  ┌────────────────────────┐  ┌──────────────────────┐   │  │
│  │  │  RSS Feed Sources      │  │  Discord API (JDA)   │   │  │
│  │  │  • HTTP Requests       │  │  • WebSocket         │   │  │
│  │  │  • XML Parsing (ROME)  │  │  • REST API Calls    │   │  │
│  │  └────────────────────────┘  └──────────────────────┘   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │         Data Persistence Layer                           │  │
│  │  ┌──────────────────────────────────────────────────────┐ │
│  │  │  SentNewsRepository (JPA)                           │ │
│  │  │  • Entity: SentNews                                 │ │
│  │  │  • Database: H2 (File-based)                        │ │
│  │  │  • Deduplication Index on 'link'                    │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Fluxo de Dados Completo

```
┌─────────────────┐
│ FeedScheduledTask
│ @Scheduled
└────────┬────────┘
         │ Triggers every N minutes/hours
         ▼
┌──────────────────────────┐
│ NewsService              │
│.fetchAndPublishNews()    │
└────────┬─────────────────┘
         │
         ├──────────────────────────────────┐
         ▼                                  ▼
┌──────────────────────────┐     ┌──────────────────────┐
│ RssFeedService           │     │ SentNewsRepository   │
│ • Fetch RSS Feeds        │     │ • Check existing     │
│ • Parse XML              │     │ • Load all URLs sent │
│ • Extract items          │     │                      │
└────────┬─────────────────┘     └──────────────────────┘
         │
         ▼
    ┌─────────────┐
    │ FeedItem[]  │
    └─────┬───────┘
         │
         ├─ Filter by criteria (optional)
         ├─ Check if already sent
         │
         ├──────────────────────────┬─────────────────────────┐
         ▼                          ▼                         ▼
    ┌────────────┐        ┌─────────────────┐      ┌────────────────┐
    │ Duplicate? │──YES──▶ Skip & Log       │      │ New & Valid?   │
    └─────┬──────┘        └─────────────────┘      └────────────────┘
         │NO                                               │YES
         │                                                 │
         └─────────────────────────┬──────────────────────┘
                                   ▼
                    ┌──────────────────────────────┐
                    │ DiscordMessageService        │
                    │ • Build Discord Embed        │
                    │ • Format Content             │
                    │ • Send to Channel (JDA)      │
                    └────────────┬─────────────────┘
                                 │
                                 ▼
                         ┌───────────────┐
                         │ Discord API   │
                         │ Message Sent! │
                         └───────────────┘
                                 │
                                 ▼
                    ┌──────────────────────────────┐
                    │ SentNewsRepository.save()    │
                    │ • Persist to H2 Database     │
                    │ • Mark as sent & timestamp   │
                    └──────────────────────────────┘
```

---

## 📦 Estrutura de Pacotes

```
com.techjournal
├── TechNewsDiscordBotApplication.java   [Main Entry Point]
│
├── config/
│   └── DiscordBotConfig.java            [Bean Configuration]
│                                         ├─ JDA Initialization
│                                         ├─ Token Validation
│                                         └─ Connection Setup
│
├── properties/
│   ├── DiscordProperties.java           [Config Binding]
│   │   ├─ token
│   │   └─ channelId
│   └── FeedProperties.java              [Config Binding]
│       └─ urls[]
│
├── entity/
│   └── SentNews.java                    [JPA Entity]
│       ├─ id (Primary Key)
│       ├─ link (Unique Index)
│       ├─ title
│       ├─ source
│       ├─ sentAt (Timestamp)
│       └─ createdAt (Timestamp)
│
├── repository/
│   └── SentNewsRepository.java          [Data Access]
│       ├─ JpaRepository<SentNews, Long>
│       ├─ findByLink()
│       └─ existsByLink()
│
├── dto/
│   └── FeedItem.java                    [Data Transfer Object]
│       ├─ title
│       ├─ description
│       ├─ link
│       ├─ imageUrl
│       ├─ publicationDate
│       ├─ source
│       ├─ author
│       └─ getSafeDescription()
│
├── service/
│   ├── RssFeedService.java              [RSS Parsing]
│   │   ├─ fetchFeeds()
│   │   ├─ fetchSingleFeed()
│   │   ├─ parseSyndEntry()
│   │   └─ extractImageUrl()
│   │
│   ├── DiscordMessageService.java       [Discord Integration]
│   │   ├─ sendNewsToDiscord()
│   │   ├─ buildNewsEmbed()
│   │   └─ truncate()
│   │
│   └── NewsService.java                 [Business Logic]
│       ├─ fetchAndPublishNews()
│       ├─ isNewNews()
│       └─ publishNews()
│
└── scheduler/
    └── FeedScheduledTask.java           [Task Scheduling]
        ├─ @Scheduled (Fixed Delay)
        └─ @Scheduled (Cron) [Optional]
```

---

## 🎯 Padrões de Design Utilizados

### 1. **Service Layer Pattern**
Separação de responsabilidades em camadas:
- **Controller/Scheduler** → Ponto de entrada
- **Service** → Lógica de negócios
- **Repository** → Acesso a dados
- **Entity** → Modelo de dados

### 2. **Dependency Injection (Spring)**
```java
@Service
public class NewsService {
    
    private final RssFeedService rssFeedService;
    private final DiscordMessageService discordMessageService;
    private final SentNewsRepository sentNewsRepository;
    
    // Constructor injection (melhor prática)
    public NewsService(RssFeedService rssFeedService,
                       DiscordMessageService discordMessageService,
                       SentNewsRepository sentNewsRepository) {
        this.rssFeedService = rssFeedService;
        this.discordMessageService = discordMessageService;
        this.sentNewsRepository = sentNewsRepository;
    }
}
```

### 3. **DAO Pattern (Repository)**
```java
public interface SentNewsRepository extends JpaRepository<SentNews, Long> {
    Optional<SentNews> findByLink(String link);
    boolean existsByLink(String link);
}
```

### 4. **Builder Pattern (DTO)**
```java
FeedItem item = FeedItem.builder()
    .title("News Title")
    .description("Description")
    .link("https://example.com")
    .build();
```

### 5. **Strategy Pattern (Feed Parsing)**
ROME fornece diferentes estratégias de parsing:
- RSS 0.91, 0.92, 0.93, 0.94, 2.0
- Atom 0.3, 1.0
- RDF

### 6. **Observer Pattern (Event Scheduling)**
Spring Scheduling com `@Scheduled` age como observer:
```java
@Scheduled(fixedDelay = 3600000)
public void execute() { }
```

### 7. **Singleton Pattern**
Beans do Spring são singletons por padrão:
```java
@Service  // Instância única por aplicação
public class NewsService { }
```

---

## 🔐 Segurança & Best Practices

### Validação de Entrada
```java
// Em DiscordBotConfig
if (discordProperties.getToken() == null || 
    discordProperties.getToken().isBlank()) {
    throw new IllegalArgumentException("Token not configured");
}
```

### Sanitização de Conteúdo
```java
// Em FeedItem
public String getSafeDescription(int maxLength) {
    String cleaned = description
        .replaceAll("<[^>]*>", "")        // Remove HTML
        .replaceAll("&amp;", "&")         // Decode entities
        .replaceAll("&lt;", "<")
        .replaceAll("&gt;", ">")
        .replaceAll("&quot;", "\"")
        .trim();
    
    if (cleaned.length() > maxLength) {
        return cleaned.substring(0, maxLength) + "...";
    }
    return cleaned;
}
```

### Tratamento de Exceções
```java
@Scheduled(...)
public void fetchAndPublishNews() {
    try {
        List<FeedItem> feedItems = rssFeedService.fetchFeeds();
        // process
    } catch (Exception e) {
        log.error("Error in news fetch and publish cycle: {}", 
                  e.getMessage(), e);
    }
}
```

### Índices de Banco de Dados
```java
@Table(indexes = {
    @Index(name = "idx_link", columnList = "link", unique = true),
    @Index(name = "idx_sent_at", columnList = "sent_at")
})
public class SentNews { }
```

---

## 📊 Diagrama de Sequência

### Caso: Publicar Nova Notícia

```
FeedScheduledTask    NewsService     RssFeedService    DiscordService    Repository    Discord API
       │                  │                  │                  │              │             │
       │─ execute()────────▶                  │                  │              │             │
       │                   │                  │                  │              │             │
       │                   │─ fetchFeeds()───▶                   │              │             │
       │                   │                  │                  │              │             │
       │                   │  ◀─ FeedItem[]──│                  │              │             │
       │                   │                  │                  │              │             │
       │                   ├─ for each item──▶                  │              │             │
       │                   │                  │                  │              │             │
       │                   ├─ isNewNews()─────────────────────────────────────▶             │
       │                   │                  │                  │              │             │
       │                   │◀─ boolean (not found)───────────────◀──────────────             │
       │                   │                  │                  │              │             │
       │                   ├─ sendNewsToDiscord()───────────────▶               │             │
       │                   │                  │                  │              │             │
       │                   │                  │      ┌─ buildEmbed()────┐     │             │
       │                   │                  │      │                  │     │             │
       │                   │                  │      └──────────────────▶             │
       │                   │                  │                  │              │             │
       │                   │                  │                  ├─ queue()────────────────▶
       │                   │                  │                  │              │             │
       │                   │                  │                  │              │        [Message Sent]
       │                   │                  │                  │              │             │
       │                   ├─ save()──────────────────────────────────────────▶             │
       │                   │                  │                  │              │             │
       │                   │                  │                  │           [Saved]        │
       │                   │                  │                  │              │             │
       │ ◀─ complete()────│                  │                  │              │             │
```

---

## 🗄️ Esquema do Banco de Dados

### Tabela: sent_news

```sql
CREATE TABLE sent_news (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    link VARCHAR(1024) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    source VARCHAR(255) NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Índices para performance
    INDEX idx_link (link) UNIQUE,
    INDEX idx_sent_at (sent_at)
);
```

### Queries Típicas

```sql
-- Verificar se notícia já foi enviada
SELECT * FROM sent_news WHERE link = 'https://example.com/news/123';

-- Contar notícias enviadas hoje
SELECT COUNT(*) FROM sent_news 
WHERE DATE(sent_at) = CURDATE();

-- Notícias mais antigas (limpeza)
SELECT * FROM sent_news 
WHERE sent_at < DATE_SUB(NOW(), INTERVAL 90 DAY);

-- Estatísticas por fonte
SELECT source, COUNT(*) as count 
FROM sent_news 
GROUP BY source 
ORDER BY count DESC;
```

---

## ⚙️ Configuração de Produção

### Ambiente Recomendado

```
┌─────────────────────────────────────┐
│ Production Environment              │
├─────────────────────────────────────┤
│ OS: Linux (Ubuntu 22.04 LTS)        │
│ JVM: OpenJDK 21 (Temurin)           │
│ Memory: 512MB heap (para esse bot)  │
│ Database: H2 (file-based) ou        │
│           PostgreSQL (escalável)    │
│ Process Manager: systemd            │
│ Reverse Proxy: nginx                │
│ Logging: ELK Stack (opcional)       │
└─────────────────────────────────────┘
```

### GC Tuning

```bash
# Para aplicações pequenas (Discord bot)
java -Xmx512m -Xms256m \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar bot.jar
```

---

## 📈 Escalabilidade & Performance

### Otimizações Implementadas

1. **Connection Pooling**
   - JPA com pool automático (HikariCP)
   - Reutiliza conexões HTTP

2. **Database Indexing**
   - Índice único em `link` (previne duplicatas)
   - Índice em `sent_at` (limpeza rápida)

3. **Lazy Loading**
   - Feeds carregados sob demanda
   - Não carrega toda a base ao iniciar

4. **Batch Processing**
   - Processa múltiplos feeds em paralelo
   - Envia para Discord de forma assíncrona

### Limitações Atuais

- **Throughput**: ~100-200 notícias/hora
- **Latência**: ~2-5 segundos entre fetch e envio
- **Storage**: ~1MB por 1000 notícias no H2
- **Memory**: ~150-200MB em operação normal

### Para Aumentar Escala

1. **Múltiplas Instâncias**
   - Load balancer com Discord webhooks
   - Message queue (RabbitMQ, Kafka)

2. **Banco de Dados Escalável**
   - PostgreSQL com sharding
   - MongoDB para logs

3. **Cache Distribuído**
   - Redis para deduplicação
   - Memcached para feeds

---

## 🧪 Testabilidade

### Estrutura para Testes

```java
@SpringBootTest
@AutoConfigureMockMvc
public class NewsServiceTest {
    
    @MockBean
    private RssFeedService feedService;
    
    @MockBean
    private SentNewsRepository repository;
    
    @Autowired
    private NewsService newsService;
    
    @Test
    public void testDuplicateDetection() {
        // Verificar se duplicatas são detectadas
    }
}
```

### Casos de Teste Recomendados

- [ ] RSS parsing com diferentes formatos
- [ ] Deduplicação de notícias
- [ ] Tratamento de feeds inacessíveis
- [ ] Validação de URLs Discord
- [ ] Limpeza de registros antigos
- [ ] Recuperação de falhas
- [ ] Performance sob carga

---

## 🔮 Possíveis Extensões Futuras

```java
// 1. Múltiplos canais por categoria
interface CategoryChannelMapping {
    String getChannelForCategory(String category);
}

// 2. Machine Learning para priorização
interface NewsRanker {
    double scoreNews(FeedItem item);
}

// 3. Webhook customizados
interface NotificationStrategy {
    void notify(FeedItem item);  // Discord, Slack, Email, etc
}

// 4. Search & Archive
interface NewsArchive {
    List<FeedItem> search(String query);
    List<FeedItem> getByDateRange(LocalDate from, LocalDate to);
}

// 5. Trending Analysis
interface TrendingAnalyzer {
    List<String> getTrendingTopics(Duration period);
}
```

---

**Arquitetura sólida, escalável e mantível! 🏗️**
