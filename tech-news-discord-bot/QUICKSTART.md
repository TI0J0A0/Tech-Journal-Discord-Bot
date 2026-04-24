# 🚀 Quick Start - Tech News Discord Bot

Guia rápido para colocar o bot funcionando em 10 minutos!

## ⚡ Passos Rápidos

### 1️⃣ Pré-requisitos (2 min)
- [x] Java 21+ instalado
- [x] Maven instalado
- [x] Acesso ao Discord Developer Portal

### 2️⃣ Obter Credenciais Discord (3 min)

**A) Criar o Bot:**
1. Acesse https://discord.com/developers/applications
2. Clique "New Application"
3. Dê um nome (ex: "Tech News Bot")
4. Vá para aba "Bot" → "Add Bot"
5. **Copie o TOKEN** (guarde bem!)
6. Em "Intents", ative:
   - ✅ MESSAGE CONTENT INTENT

**B) Autorizar no Servidor:**
1. Em "OAuth2" → "URL Generator"
2. Selecione scope: `bot`
3. Selecione permissions:
   - ✅ Send Messages
   - ✅ Embed Links
4. Copie a URL gerada
5. Abra no navegador para adicionar ao seu servidor

**C) Obter ID do Canal:**
1. No Discord, ative "Developer Mode":
   - User Settings → Advanced → Developer Mode ON
2. Clique direito no canal de notícias
3. "Copy Channel ID" (guarde!)

### 3️⃣ Configurar o Projeto (3 min)

**Opção A: Variáveis de Ambiente (Recomendado)**

Windows (PowerShell):
```powershell
$token = "seu_token_aqui"
$channelId = "seu_id_canal_aqui"

[Environment]::SetEnvironmentVariable("DISCORD_BOT_TOKEN", $token, "User")
[Environment]::SetEnvironmentVariable("DISCORD_CHANNEL_ID", $channelId, "User")
```

Linux/Mac (Bash):
```bash
export DISCORD_BOT_TOKEN="seu_token_aqui"
export DISCORD_CHANNEL_ID="seu_id_canal_aqui"
```

**Opção B: Arquivo de Configuração**

Crie `src/main/resources/application-local.properties`:
```properties
app.discord.token=seu_token_aqui
app.discord.channel-id=seu_id_canal_aqui
```

### 4️⃣ Build & Run (2 min)

```bash
# No diretório do projeto
mvn clean install
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

Ou se usou variáveis de ambiente:
```bash
mvn clean install
mvn spring-boot:run
```

### 5️⃣ Confirmar Funcionamento

Procure no console por:
```
Discord Bot successfully connected as: YourBotName#0000
```

E depois:
```
============ News Fetch Task Started ============
Published X new articles to Discord
============ News Fetch Task Completed ============
```

✅ **Pronto!** Seu bot está rodando!

---

## 🎯 Verificação Rápida

| Passo | Status | ✓ |
|-------|--------|---|
| Java 21+ instalado | `java -version` | |
| Maven instalado | `mvn -version` | |
| Bot criado no Discord | No Developer Portal | |
| TOKEN copiado | Seguro em variável | |
| Channel ID obtido | Seguro em variável | |
| Projeto buildado | `mvn clean install` | |
| Bot online | Vê no Discord | |
| Mensagens enviadas | No canal | |

---

## 🔍 Troubleshooting Rápido

### ❌ "Token not configured"
```bash
# Windows PowerShell - Verificar
$Env:DISCORD_BOT_TOKEN
# Se vazio, execute novamente o SetEnvironmentVariable

# Linux/Mac - Verificar
echo $DISCORD_BOT_TOKEN
# Se vazio, execute novamente o export
```

### ❌ "Channel with ID not found"
1. Verifique o Channel ID (botão direito no canal)
2. Certifique-se que o bot tem permissão de enviar mensagens
3. Use a mesma variável em todos os lugares

### ❌ Bot não envia mensagens
1. Cheque o console procurando por erros
2. Verifique permissões do bot no servidor
3. Veja se o channel ID está correto

### ❌ "jdk.compiler module not found"
```bash
# Certifique-se que tem Java 21+
java -version
# Se estiver em 17 ou menor, baixe Java 21
```

---

## 📝 Próximos Passos

### Personalizar Feeds
Edite `src/main/resources/application.properties`:
```properties
app.feeds.urls[0]=https://seu-feed-rss-aqui
app.feeds.urls[1]=https://outro-feed-rss
```

### Mudar Intervalo de Verificação
```properties
# Em milissegundos
app.scheduler.fixed-delay=1800000  # 30 minutos
# ou
app.scheduler.fixed-delay=600000   # 10 minutos
```

### Deploy em Produção
Veja seção "Deploy em Produção" no README.md

---

## 💡 Dicas Profissionais

1. **Use Variáveis de Ambiente** para produção
   - Nunca commit de tokens no Git!
   - Use `.env` para desenvolvimento local

2. **Monitore os Logs**
   ```bash
   tail -f logs/bot.log
   ```

3. **Teste os Feeds**
   - Visite as URLs dos feeds no navegador
   - Verifique se o XML carrega corretamente

4. **Ajuste o Intervalo**
   - Comece com 30-60 minutos
   - Não spam de feeds (riscos de rate limit)

5. **Verifique Permissões**
   - Bot precisa de "Send Messages" e "Embed Links"
   - Admin do servidor pode checar em Role Permissions

---

## 📞 Suporte Rápido

| Problema | Solução |
|----------|---------|
| Token inválido | Gere novo em Developer Portal |
| Channel ID errado | Copy again com Developer Mode |
| Build falha | `mvn clean` e tentar novamente |
| Bot offline | Verifique logs de erro no console |
| Nenhuma msg no Discord | Verifique permissões do bot |

---

**Tudo pronto? Divirta-se com seu novo Tech News Bot! 🎉**
