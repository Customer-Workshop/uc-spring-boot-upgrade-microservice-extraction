# Migration Notes: Articles API (Java Spring Boot → Python FastAPI)

## Overview

This document describes the translation of the Articles REST API from the Java Spring Boot monolith to a standalone Python FastAPI microservice.

## API Parity

| Endpoint | Spring Boot | FastAPI | Status |
|----------|------------|---------|--------|
| `POST /articles` | `ArticlesApi.createArticle()` | `router.create_article()` | Implemented |
| `GET /articles` | `ArticlesApi.getArticles()` | `router.list_articles()` | Implemented |
| `GET /articles/{slug}` | `ArticleApi.article()` | `router.get_article()` | Implemented |
| `PUT /articles/{slug}` | `ArticleApi.updateArticle()` | `router.update_article()` | Implemented |
| `DELETE /articles/{slug}` | `ArticleApi.deleteArticle()` | `router.delete_article()` | Implemented |
| `GET /tags` | `TagsApi.getTags()` | `main.get_tags()` | Implemented |
| `GET /articles/feed` | `ArticlesApi.getFeed()` | Not implemented | Deferred (requires follow system) |

## Key Differences

### 1. ORM Layer
- **Java**: MyBatis (Data Mapper pattern) with XML-based SQL mappings
- **Python**: SQLAlchemy 2.0 (Active Record + Unit of Work pattern) with declarative models

### 2. Authentication
- **Java**: Spring Security + JWT token filter with `@AuthenticationPrincipal`
- **Python**: Simplified for microservice demo; uses first user in DB. Production would use FastAPI `Depends()` with JWT middleware.

### 3. Slug Generation
- **Java**: Custom `Article.toSlug()` using UUID suffix
- **Python**: `python-slugify` library for URL-friendly slug generation

### 4. Validation
- **Java**: Jakarta Bean Validation (`@Valid`, `@NotBlank`) with custom constraint validators (`DuplicatedArticleConstraint`)
- **Python**: Pydantic models with built-in validation; duplicate check in route handler

### 5. Database
- **Java**: SQLite via JDBC with Flyway migrations
- **Python**: SQLite via SQLAlchemy with auto-schema creation (`create_all`)

### 6. Response Format
Both use the same JSON envelope format:
```json
{
  "article": { "slug": "...", "title": "...", ... },
  "articles": [...], "articlesCount": N
}
```

## Test Coverage

10 pytest tests covering:
- CRUD operations (create, read, update, delete)
- Tag filtering and author filtering
- Pagination (offset/limit)
- Duplicate title rejection (422)
- 404 for nonexistent articles
- Tags endpoint

## Running the Microservice

```bash
cd articles-fastapi
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8082
```

## Running Tests

```bash
cd articles-fastapi
pip install -r requirements.txt
pytest tests/ -v
```
