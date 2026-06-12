# AI Chatbot Backend

Backend Spring Boot 3.x du chatbot IA avec mémoire conversationnelle PostgreSQL.

## Architecture

- `ChatController` expose `POST /api/chat`.
- `ConversationController` expose la liste, les messages et la suppression des conversations.
- `ChatService` crée ou charge une conversation, persiste les messages et construit le prompt avec un historique limité.
- `LlmClientService` encapsule l'appel HTTP vers l'API LLM compatible OpenAI.
- `AiProperties` centralise la configuration du provider IA.
- `GlobalExceptionHandler` gère les erreurs de validation, d'indisponibilité IA et les erreurs inattendues.

Flux cible :

```text
Angular -> Spring Boot -> PostgreSQL -> LLM provider -> PostgreSQL -> Angular
```

## Configuration

La clé API ne doit jamais être hardcodée. Elle est lue depuis la variable d'environnement `OPENAI_API_KEY`.

```bash
export OPENAI_API_KEY="sk-..."
```

Configuration par défaut dans `src/main/resources/application.yml` :

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ai_chatbot
    username: ai_chatbot
    password: ai_chatbot
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false

ai:
  provider:
    base-url: https://api.openai.com/v1
    api-key: ${OPENAI_API_KEY}
    model: gpt-4.1-mini
    timeout-seconds: 60
```

## Lancer PostgreSQL

```bash
docker compose up -d
```

## Lancer le backend

```bash
mvn spring-boot:run
```

Le backend démarre sur `http://localhost:8080`.

## Déploiement Render

Ce repo contient un `render.yaml` qui décrit deux services Render :

- `ai-chatbot-backend` : web service Docker Spring Boot.
- `ai-chatbot-frontend` : static site Angular depuis `HanPass/ai-chatbot-frontend`.

Dans Render, créer un Blueprint depuis ce repo puis renseigner la variable secrète `OPENAI_API_KEY`.

## Tester avec curl

Créer une nouvelle conversation :

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Explique-moi une NullPointerException en Java"}'
```

Réponse attendue :

```json
{
  "conversationId": "00000000-0000-0000-0000-000000000000",
  "answer": "Une NullPointerException arrive lorsque..."
}
```

Envoyer un message dans une conversation existante :

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"conversationId":"00000000-0000-0000-0000-000000000000","message":"Donne-moi un exemple"}'
```

Lister les conversations :

```bash
curl http://localhost:8080/api/conversations
```

Lister les messages d'une conversation :

```bash
curl http://localhost:8080/api/conversations/00000000-0000-0000-0000-000000000000/messages
```

Supprimer une conversation :

```bash
curl -X DELETE http://localhost:8080/api/conversations/00000000-0000-0000-0000-000000000000
```

## Tests

```bash
mvn test
```

## Points d'extension

- Ajouter un historique conversationnel en enrichissant `ChatRequest`.
- Ajouter le streaming avec Server-Sent Events ou WebSocket.
- Ajouter un RAG en amont de `ChatService`.
- Ajouter des tools via une couche d'orchestration dédiée.
- Ajouter des migrations Flyway/Liquibase si le schéma doit être versionné finement.
