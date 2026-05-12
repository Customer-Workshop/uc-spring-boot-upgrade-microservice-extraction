# API Configuration Patch

## Current State

The frontend API base URL is hardcoded in `frontend/lib/utils/constant.ts`:

```typescript
export const SERVER_BASE_URL = `http://localhost:8080`;
```

This constant is imported and used across the entire frontend codebase:
- `frontend/lib/api/article.ts`
- `frontend/lib/api/user.ts`
- `frontend/lib/api/comment.ts`
- `frontend/lib/api/tag.ts`
- `frontend/pages/profile/[pid].tsx`
- `frontend/pages/article/[pid].tsx`
- `frontend/pages/editor/[pid].tsx`
- `frontend/components/comment/CommentList.tsx`
- `frontend/components/comment/CommentInput.tsx`
- `frontend/components/comment/DeleteButton.tsx`
- `frontend/components/home/Tags.tsx`
- `frontend/components/article/ArticleList.tsx`
- `frontend/components/article/ArticleActions.tsx`

## Required Change

To make the API URL configurable via the `NEXT_PUBLIC_API_URL` environment variable, modify `frontend/lib/utils/constant.ts`:

### Before
```typescript
export const SERVER_BASE_URL = `http://localhost:8080`;
```

### After
```typescript
export const SERVER_BASE_URL = process.env.NEXT_PUBLIC_API_URL || `http://localhost:8080`;
```

## How It Works

- `NEXT_PUBLIC_` prefix makes the variable available at build time in Next.js (inlined during `next build`)
- The Dockerfile sets `ENV NEXT_PUBLIC_API_URL=/api` before `npm run build`, so the built frontend uses `/api` as the base URL
- Relative URL `/api` means requests go to the same origin, which nginx then proxies to the backend microservices
- The fallback `http://localhost:8080` preserves existing local development behavior

## No Other Files Need Changes

Since all API modules import `SERVER_BASE_URL` from `constant.ts`, changing this single file makes the entire frontend configurable.
