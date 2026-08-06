# Contract: `GET /tags/stats` (DJ-86)

Single source of truth for the backend and frontend workstreams. Neither side may change
this unilaterally.

## Request

```
GET /tags/stats
```

- Public: no authentication, no `Authorization` header required.
- No query parameters, no pagination.

## Response

`200 OK`, `Content-Type: application/json`

```json
{
  "tags": [
    { "name": "java", "articleCount": 12 },
    { "name": "spring", "articleCount": 7 },
    { "name": "unused", "articleCount": 0 }
  ]
}
```

- `tags` is always present and is an array (empty array when no tags exist).
- `name`: tag name, non-empty string.
- `articleCount`: non-negative integer; number of articles carrying the tag.
- Ordering: `articleCount` descending, then `name` ascending.
- Tags with no articles are included with `articleCount: 0`.
- Data source: `tags` left-joined with `article_tags`
  (`src/main/resources/db/migration/V1__create_tables.sql`).

## Java DTO (backend)

```java
// io.spring.application.TagStatsData
public class TagStatsData {
  private String name;
  private int articleCount;
}
```

Envelope built the same way as `TagsApi.getTags()` — a `{"tags": ...}` map.

## TypeScript type (frontend)

```ts
export interface TagStats {
  name: string;
  articleCount: number;
}

export interface TagStatsResponse {
  tags: TagStats[];
}
```

Accessed via `TagAPI.getStats()` / SWR against `${SERVER_BASE_URL}/tags/stats`.
