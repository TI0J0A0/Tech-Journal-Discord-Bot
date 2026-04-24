# 🤖 Tech News Discord Bot

Um bot de Discord profissional que distribui automaticamente notícias de tecnologia e cibersegurança via RSS feeds. O bot busca conteúdo de múltiplas fontes confiáveis e os publica em tempo real em um canal específico do Discord com formatação visual atraente.

## ✨ Funcionalidades Principais

- ✅ **Leitura de RSS**: Suporta múltiplos feeds RSS/Atom de diferentes fontes
- ✅ **Publicação Automática**: Agenda verificações periódicas dos feeds configuráveis
- ✅ **Discord Embeds**: Mensagens visualmente atraentes com título, descrição, imagem e link
- ✅ **Controle de Duplicatas**: Mantém histórico de notícias publicadas para evitar repetições
- ✅ **Banco de Dados Leve**: H2 Database para armazenamento local e rápido
- ✅ **Logging Detalhado**: Logs estruturados para monitoramento e debugging
- ✅ **Configuração Externa**: Variáveis de ambiente e arquivo de properties
- ✅ **Tratamento de Erros**: Sistema robusto de tratamento de exceções

## 🛠️ Stack Tecnológico

```
┌─────────────────────────────────────┐
│ Java 21 + Spring Boot 3.2.3          │
├─────────────────────────────────────┤
│ JDA 5.0.0 (Java Discord API)        │
│ ROME 2.1.0 (RSS Parser)              │
│ H2 Database (Persistência)           │
│ Maven (Build & Dependency Mgmt)     │
└─────────────────────────────────────┘
```

### Versões
- **Java**: 21+ (LTS)
- **Spring Boot**: 3.2.3
- **JDA**: 5.0.0-beta.24
- **ROME**: 2.1.0
- **H2**: Latest

## 📋 Pré-requisitos

### Sistema
- Java 21+ instalado
- Maven 3.8.0+
- Acesso à internet (para fetch de RSS)

### Discord
1. **Criar um Discord Server** (se não tiver)
2. **Criar um Bot no Discord Developer Portal**:
   - Acesse: https://discord.com/developers/applications
   - Clique em "New Application"
   - Vá para a aba "Bot" e clique "Add Bot"
   - Copie o **TOKEN** (guardará para depois)
   - Em "MESSAGE CONTENT INTENT", clique para ativar

3. **Configurar Permissões**:
   - Em "OAuth2 > URL Generator", selecione:
     - Scopes: `bot`
     - Permissions: `Send Messages`, `Embed Links`, `Read Message History`, `View Channels`
   - Copie a URL gerada e abra no navegador para adicionar o bot ao seu servidor

4. **Obter ID do Canal**:
   - No Discord, ative "Developer Mode" (User Settings > Advanced > Developer Mode)
   - Clique com botão direito no canal desejado
   - Selecione "Copy Channel ID"

## 🚀 Instalação e Setup

### 1. Clone ou baixe o projeto

```bash
cd tech-news-discord-bot
```

### 2. Configure as variáveis de ambiente

**Opção A: Variáveis de Ambiente do Sistema**

No Windows (PowerShell):
```powershell
[Environment]::SetEnvironmentVariable("DISCORD_BOT_TOKEN", "seu_token_aqui", "User")
[Environment]::SetEnvironmentVariable("DISCORD_CHANNEL_ID", "seu_id_canal_aqui", "User")
```

No Linux/Mac (Bash):
```bash
export DISCORD_BOT_TOKEN="seu_token_aqui"
export DISCORD_CHANNEL_ID="seu_id_canal_aqui"
```

**Opção B: Arquivo application-local.properties** (Recomendado para desenvolvimento)

Crie o arquivo `src/main/resources/application-local.properties`:

```properties
app.discord.token=seu_token_aqui
app.discord.channel-id=seu_id_canal_aqui
```

Execute com:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

**Opção C: Editar application.properties** (Não recomendado para produção)

```properties
app.discord.token=seu_token_aqui
app.discord.channel-id=seu_id_canal_aqui
```

### 3. Configurar RSS Feeds (Opcional)

Edite `src/main/resources/application.properties`:

```properties
app.feeds.urls[0]=https://sua-primeira-url-feed-rss
app.feeds.urls[1]=https://sua-segunda-url-feed-rss
app.feeds.urls[2]=https://sua-terceira-url-feed-rss
```

**Feeds recomendados para Tech & Security:**
- TechCrunch: `https://feeds.techcrunch.com/techcrunch/`
- ArsTechnica: `https://feeds.arstechnica.com/arstechnica/index`
- The Hacker News: `https://thehackernews.com/feeds/posts/default?alt=rss`
- SecurityWeek: `https://feeds.securityweek.com/securityweek`
- Infosecurity Magazine: `https://www.infosecurity-magazine.com/feed/rss/`

### 4. Configurar Intervalo de Sincronização

Edite `src/main/resources/application.properties`:

```properties
# Em milissegundos
app.scheduler.fixed-delay=3600000  # 1 hora
# ou
app.scheduler.fixed-delay=1800000  # 30 minutos
# ou
app.scheduler.fixed-delay=600000   # 10 minutos
```

### 5. Build do Projeto

```bash
mvn clean install
```

### 6. Executar a Aplicação

```bash
mvn spring-boot:run
```

Ou, após o build:

```bash
java -jar target/tech-news-discord-bot-1.0.0.jar
```

## 📝 Estrutura do Projeto

```
tech-news-discord-bot/
├── pom.xml                                    # Dependências Maven
├── README.md                                  # Este arquivo
├── .gitignore                                 # Git ignore
│
└── src/main/
    ├── java/com/techjournal/
    │   ├── TechNewsDiscordBotApplication.java # Classe principal
    │   │
    │   ├── config/
    │   │   └── DiscordBotConfig.java         # Configuração JDA
    │   │
    │   ├── properties/
    │   │   ├── DiscordProperties.java        # Props Discord
    │   │   └── FeedProperties.java           # Props RSS
    │   │
    │   ├── entity/
    │   │   └── SentNews.java                 # Modelo de dados
    │   │
    │   ├── repository/
    │   │   └── SentNewsRepository.java       # Acesso a dados
    │   │
    │   ├── dto/
    │   │   └── FeedItem.java                 # Data Transfer Object
    │   │
    │   ├── service/
    │   │   ├── RssFeedService.java           # Parser de RSS
    │   │   ├── DiscordMessageService.java    # Envio ao Discord
    │   │   └── NewsService.java              # Orquestrador
    │   │
    │   └── scheduler/
    │       └── FeedScheduledTask.java        # Tarefa agendada
    │
    └── resources/
        ├── application.properties             # Configuração padrão
        └── application-local.properties       # Config local (git ignored)

└── data/
    └── techjournal.h2.db                      # Database H2
```

## 🔧 Configuração Avançada

### Usando CRON em vez de Fixed Delay

Se preferir horários específicos, edite `FeedScheduledTask.java`:

```java
@Scheduled(cron = "0 0 */4 * * ?")  // A cada 4 horas
public void fetchAndPublishNews() {
    newsService.fetchAndPublishNews();
}
```

**Exemplos de CRON:**
- `0 0 * * * ?` - Meia-noite todos os dias
- `0 9,14,19 * * ?` - 9h, 14h e 19h todos os dias
- `0 */6 * * * ?` - A cada 6 horas
- `0 0 * * 1` - Segunda-feira à meia-noite

### Logging

Edite `application.properties`:

```properties
logging.level.com.techjournal=DEBUG        # Debug detalhado
logging.level.org.springframework.boot=WARN # Spring menos verboso
logging.file.name=logs/bot.log             # Salvar em arquivo
```

### Aumentar tamanho da Description

Edite `FeedItem.java`:

```java
public String getSafeDescription(int maxLength) {
    // Altere maxLength na chamada em DiscordMessageService
}
```

Em `DiscordMessageService.java`:

```java
embed.setDescription(feedItem.getSafeDescription(500)); // Aumentar de 300 para 500
```

## 🎯 Utilizando o Bot

### Inicialização

1. Execute o projeto (ver seção de Build)
2. Observe os logs para confirmação:
   ```
   Discord Bot successfully connected as: TechBot#1234
   ```

3. O bot aguardará 5 segundos (initial-delay) antes da primeira busca
4. A cada 1 hora (ou intervalo configurado), o bot buscará novos feeds

### Comportamento Esperado

```
============ News Fetch Task Started ============
Fetched 15 items from feed: https://feeds.techcrunch.com/techcrunch/
Fetched 8 items from feed: https://thehackernews.com/feeds/posts/default
Total items fetched: 23
Published 5 new articles to Discord
============ News Fetch Task Completed ============
```

### Visualização no Discord

O bot enviará mensagens formatadas assim:

```
┌─────────────────────────────────────────────┐
│ [TÍTULO DA NOTÍCIA]                         │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │
│ [DESCRIÇÃO/RESUMO DA NOTÍCIA]               │
│                                             │
│ [IMAGEM DA NOTÍCIA]                         │
│                                             │
│ Fonte         │ TechCrunch                  │
│ Autor         │ Jane Doe                    │
│ Hora          │ 2024-04-23 14:30 UTC        │
│                                             │
│ Tech News Discord Bot | Leia a notícia...  │
└─────────────────────────────────────────────┘
```

## 🐛 Troubleshooting

### Problema: "Discord bot token not configured"

**Solução:**
```bash
# Verificar se a variável está setada
echo $DISCORD_BOT_TOKEN  # Linux/Mac
echo $Env:DISCORD_BOT_TOKEN  # PowerShell

# Se não aparecer, defina novamente
export DISCORD_BOT_TOKEN="seu_token"
```

### Problema: "Channel with ID not found"

**Solução:**
1. Verifique se o ID do canal está correto
2. Certifique-se de que o bot tem permissão de enviar mensagens no canal
3. Ative "Developer Mode" no Discord e copie o ID novamente

### Problema: Bot não envia mensagens

**Verificar:**
1. Logs da aplicação (procure por erros)
2. Se o bot está online no Discord
3. Se o canal está mencionado corretamente
4. Permissões do bot no servidor

### Problema: "The specified intents are not enabled"

**Solução:** Ative MESSAGE_CONTENT_INTENT no Discord Developer Portal

### Problema: Feeds não carregam

**Verificar:**
1. URLs dos feeds estão corretas e acessíveis
2. Firewall permite acesso externo
3. Logs mostram o erro específico

## 📊 Monitoramento

### Arquivos de Log

```bash
# Ver logs em tempo real
tail -f logs/bot.log

# Buscar erros
grep ERROR logs/bot.log

# Buscar execuções do agendador
grep "News Fetch Task" logs/bot.log
```

### Verificar Banco de Dados

Use o H2 Console (opcional):

```properties
# Em application.properties
spring.h2.console.enabled=true
# Acesse: http://localhost:8080/h2-console
```

## 🔐 Segurança

### Boas Práticas

✅ **Use variáveis de ambiente** para tokens (nunca commit no Git)
✅ **Nunca compartilhe tokens** publicamente
✅ **Validate URLs** de RSS antes de adicionar
✅ **Use HTTPS** para feeds
✅ **Limite permissões** do bot apenas ao necessário
✅ **Monitore logs** regularmente

### .gitignore essencial

```
application-local.properties
.env
*.log
data/
```

## 🚀 Deploy em Produção

### Docker (Opcional)

Crie um `Dockerfile`:

```dockerfile
FROM openjdk:21-slim
COPY target/tech-news-discord-bot-1.0.0.jar app.jar
ENV DISCORD_BOT_TOKEN=${DISCORD_BOT_TOKEN}
ENV DISCORD_CHANNEL_ID=${DISCORD_CHANNEL_ID}
ENTRYPOINT ["java","-jar","app.jar"]
```

Build e run:

```bash
docker build -t tech-news-bot .
docker run -e DISCORD_BOT_TOKEN=xxx -e DISCORD_CHANNEL_ID=yyy tech-news-bot
```

### Systemd Service (Linux)

Crie `/etc/systemd/system/tech-news-bot.service`:

```ini
[Unit]
Description=Tech News Discord Bot
After=network.target

[Service]
Type=simple
User=botuser
Environment="DISCORD_BOT_TOKEN=seu_token"
Environment="DISCORD_CHANNEL_ID=seu_id"
ExecStart=/usr/bin/java -jar /opt/bot/tech-news-discord-bot-1.0.0.jar
Restart=always
RestartSec=30

[Install]
WantedBy=multi-user.target
```

Enable e start:

```bash
sudo systemctl enable tech-news-bot
sudo systemctl start tech-news-bot
sudo systemctl status tech-news-bot
```

## 📖 Documentação Técnica

### Classes Principais

#### `TechNewsDiscordBotApplication`
- Classe de inicialização Spring Boot
- Ativa scheduling com `@EnableScheduling`

#### `DiscordBotConfig`
- Configura e inicializa JDA
- Valida token e channel ID
- Aguarda conexão pronta

#### `RssFeedService`
- Busca feeds RSS de múltiplas URLs
- Faz parsing com ROME
- Extrai imagens do conteúdo

#### `DiscordMessageService`
- Formata mensagens com EmbedBuilder
- Envia ao canal específico
- Trata truncamento de texto

#### `NewsService`
- Orquestra todo o fluxo
- Verifica duplicatas
- Persiste notícias enviadas

#### `FeedScheduledTask`
- Agenda execução periódica
- Dispara a cada X minutos/horas

### Fluxo de Dados

```
FeedScheduledTask.fetchAndPublishNews()
         ↓
NewsService.fetchAndPublishNews()
         ↓
RssFeedService.fetchFeeds() ────────┐
         ↓                           │
parse RSS/Atom ◄────────────────────┘
         ↓
FeedItem[]
         ↓
NewsService.publishNews(FeedItem)
         ↓
    ┌────┴────┐
    ↓         ↓
Discord    Database
(JDA)      (H2)
```

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.

## 💬 Suporte

Para dúvidas ou issues:
1. Consulte a seção Troubleshooting
2. Verifique os logs (`app.scheduler` e `com.techjournal`)
3. Valide suas configurações em `application.properties`

## 📚 Recursos Adicionais

- [JDA Documentation](https://jda.wiki)
- [ROME Podcast Module](https://rometools.jira.com/wiki/display/ROME)
- [Spring Boot Scheduling](https://spring.io/guides/gs/scheduling-tasks/)
- [Discord Developer Portal](https://discord.com/developers/docs)
- [RSS Specification](https://www.rssboard.org/rss-specification)

---

**Desenvolvido com ❤️ para entusiastas de tecnologia e cibersegurança**

Última atualização: Abril 2024 | v1.0.0
