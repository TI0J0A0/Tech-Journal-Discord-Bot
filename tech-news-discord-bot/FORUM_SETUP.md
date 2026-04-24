# 📋 Configuração do Canal de Fórum

Guia rápido para configurar o **Canal de Fórum** do Discord para receber as notícias.

---

## 🎯 Como Ficará

Cada notícia vira um **post separado** no fórum:

```
📋 Tech Journal (Fórum)
│
├─ 🔒 Critical Zero-Day Vulnerability Found in... ← Post #1 (thread)
│   └─ [🔒 Segurança] • 📡 The Hacker News • 🕐 23/04/2026 às 14:30
│       [Embed com imagem, descrição, link]
│
├─ 🤖 OpenAI Announces GPT-5 with Reasoning...    ← Post #2 (thread)
│   └─ [🤖 IA] • 📡 TechCrunch • 🕐 23/04/2026 às 14:15
│       [Embed com imagem, descrição, link]
│
└─ 💻 Apple Releases M5 Chip with 30% Faster...   ← Post #3 (thread)
    └─ [💻 Tech] • 📡 Ars Technica • 🕐 23/04/2026 às 14:00
        [Embed com imagem, descrição, link]
```

Usuários podem **comentar em cada notícia separadamente** dentro da thread.

---

## 🛠️ Passo 1 — Criar o Canal de Fórum

No Discord:

1. Clique com botão direito no servidor → **"Criar Canal"**
2. Selecione o tipo: **Fórum** (ícone de lista)
3. Nomeie (ex: `tech-news`, `noticias-tech`, `feed-seguranca`)
4. Clique em **"Criar Canal"**

---

## 🏷️ Passo 2 — Criar Tags (Recomendado)

O bot **aplica tags automaticamente** se elas existirem no fórum. Crie tags com estes nomes para aproveitar:

| Tag a criar | Emoji sugerido | Cor sugerida |
|-------------|----------------|--------------|
| `Segurança` | 🔒 | Vermelho |
| `IA` | 🤖 | Roxo |
| `Tech` | 💻 | Azul |
| `Negócios` | 💼 | Verde |
| `Geral` | 📰 | Cinza |

**Como criar tags:**

1. Clique com botão direito no canal de fórum → **"Editar Canal"**
2. Na aba lateral, clique em **"Tags"** (ou **"Marcadores de Post"**)
3. Clique em **"Criar Tag"**
4. Preencha:
   - **Nome**: `Segurança` (exatamente assim, com acento)
   - **Emoji**: 🔒
   - ☐ Moderators Only (deixe desmarcado para o bot aplicar)
5. Repita para cada tag
6. Clique em **"Salvar Alterações"**

> 💡 **Como o match funciona:** o bot procura tags cujo nome **contenha** o nome da categoria (case-insensitive). Então `Segurança`, `🔒 Segurança`, ou `Segurança Cibernética` todas funcionam.

---

## 🆔 Passo 3 — Copiar o ID do Canal de Fórum

1. Ative **Developer Mode** no Discord:
   - Configurações do Usuário → Avançado → **Modo Desenvolvedor** ✅
2. Clique com botão direito no **canal de fórum** → **"Copiar ID do Canal"**
3. Cole no seu `application.properties` ou variável de ambiente:

```powershell
[Environment]::SetEnvironmentVariable("DISCORD_CHANNEL_ID", "1234567890123456789", "User")
```

⚠️ **Certifique-se** de que é o ID do canal de **FÓRUM**, não de texto. Se pegar errado, o log mostrará:

```
Forum channel with ID xxx not found. Verifique se o ID é de um canal de FÓRUM (não de texto).
```

---

## 🔐 Passo 4 — Permissões do Bot

O bot precisa das seguintes permissões **no canal de fórum**:

- ✅ **Ver Canal** (View Channel)
- ✅ **Criar Posts** (Create Posts)
- ✅ **Enviar Mensagens em Threads** (Send Messages in Threads)
- ✅ **Incorporar Links** (Embed Links)
- ✅ **Anexar Arquivos** (Attach Files) — opcional, para imagens externas

**Como conceder:**

1. Botão direito no servidor → **"Configurações do Servidor"** → **"Cargos"**
2. Selecione o cargo do bot → aba **"Permissões"**
3. Ative as permissões acima

**OU** no canal específico:
1. Botão direito no fórum → **"Editar Canal"** → **"Permissões"**
2. Adicione o cargo do bot → conceda as permissões

---

## 🎨 Exemplo Visual do Post

**Título do post no fórum:**
```
🔒 Critical Zero-Day Vulnerability Found in Apache Struts
```

**Tags aplicadas:** `🔒 Segurança`

**Conteúdo do post (primeira mensagem):**

```
> 🔒 Segurança • 📡 The Hacker News • 🕐 23/04/2026 às 14:30

┌──────────────────────────────────────────────┐
│ [Barra vermelha à esquerda]                  │
│                                              │
│ Critical Zero-Day Vulnerability Found in    │
│ Apache Struts                                │
│ ─────────────────────────────────────────── │
│                                              │
│ A newly discovered critical vulnerability   │
│ (CVE-2026-12345) in Apache Struts allows    │
│ remote code execution without authentication│
│ ...                                          │
│                                              │
│ [IMAGEM DA NOTÍCIA]                         │
│                                              │
│ 📡 Fonte        │ ✍️ Autor      │ 🏷️ Categoria│
│ The Hacker News │ John Doe     │ 🔒 Segurança│
│                                              │
│ Tech Journal • Clique no título para ler... │
└──────────────────────────────────────────────┘
```

---

## 🧪 Como Testar

1. Configure tudo (canal + tags + ID + token)
2. Rode: `mvn spring-boot:run`
3. Aguarde 5 segundos (initial-delay)
4. O bot fará a primeira busca de feeds
5. Verifique o canal de fórum — deve aparecer um post para cada notícia nova
6. No console, você verá:

```
[Segurança] Post criado no fórum: Critical Zero-Day...
[Tech] Post criado no fórum: Apple Releases M5 Chip...
[IA] Post criado no fórum: OpenAI Announces GPT-5...
```

---

## ❓ Troubleshooting

| Problema | Solução |
|----------|---------|
| `Forum channel with ID xxx not found` | Verificar se o ID é de fórum, não texto |
| Posts sem tags | Crie tags no fórum com nomes: `Segurança`, `IA`, `Tech`, `Negócios`, `Geral` |
| `Missing Permissions` | Conceder permissões listadas no Passo 4 |
| Post sem imagem | Nem todo feed tem imagem no RSS (não é erro) |
| Notícia duplicada | Verifique H2 — banco em `./data/techjournal.h2.db` |

---

## 🎯 Regras de Categorização

O bot classifica cada notícia automaticamente:

| Categoria | Acionada quando... |
|-----------|-------------------|
| 🔒 **Segurança** | Fonte contém `hacker news`, `security`, `dark reading`, `krebs` OU texto tem `vulnerability`, `malware`, `ransomware`, `exploit`, `cve-`, `zero-day`, etc |
| 🤖 **IA** | Texto contém `openai`, `chatgpt`, `gpt`, `anthropic`, `claude`, `gemini`, `llm`, `machine learning`, etc |
| 💼 **Negócios** | Texto contém `funding`, `acquisition`, `ipo`, `merger`, `startup raises`, `series a/b/c`, etc |
| 💻 **Tech** | Fonte é `techcrunch`, `ars technica`, `verge`, `wired` |
| 📰 **Geral** | Nenhuma das anteriores |

Quer customizar? Edite [NewsCategorizer.java](src/main/java/com/techjournal/service/NewsCategorizer.java).
