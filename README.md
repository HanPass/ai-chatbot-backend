# AI Chatbot Backend

Backend Spring Boot 3.x du MVP chatbot IA.

## Architecture

- `ChatController` expose `POST /api/chat`.
- `ChatService` construit le prompt système/utilisateur.
- `LlmClientService` encapsule l'appel HTTP vers l'API LLM compatible OpenAI.
- `AiProperties` centralise la configuration du provider IA.
- `GlobalExceptionHandler` gère les erreurs de validation, d'indisponibilité IA et les erreurs inattendues.

Flux cible :

```text
Angular -> Spring Boot /api/chat -> LLM provider -> Spring Boot -> Angular
```

## Configuration

La clé API ne doit jamais être hardcodée. Elle est lue depuis la variable d'environnement `OPENAI_API_KEY`.

```bash
export OPENAI_API_KEY="sk-..."
```

Configuration par défaut dans `src/main/resources/application.yml` :

```yaml
ai:
  provider:
    base-url: https://api.openai.com/v1
    api-key: ${OPENAI_API_KEY}
    model: gpt-4.1-mini
    timeout-seconds: 60
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

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Explique-moi une NullPointerException en Java"}'
```

Réponse attendue :

```json
{
  "answer": "Une NullPointerException arrive lorsque..."
}
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
- Ajouter une persistance quand le besoin métier est confirmé.
