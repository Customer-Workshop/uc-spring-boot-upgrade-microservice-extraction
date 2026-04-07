/** Profile shape in JSON responses — mirrors Java ProfileData (id excluded via @JsonIgnore) */
export interface ProfileResponse {
  username: string;
  bio: string | null;
  image: string | null;
  following: boolean;
}

/** Single article shape in JSON responses — mirrors Java ArticleData */
export interface ArticleResponse {
  id: string;
  slug: string;
  title: string;
  description: string;
  body: string;
  favorited: boolean;
  favoritesCount: number;
  createdAt: string; // ISO 8601
  updatedAt: string; // ISO 8601
  tagList: string[];
  author: ProfileResponse;
}

/** Wrapper for single article endpoint responses */
export interface SingleArticleResponse {
  article: ArticleResponse;
}

/** Wrapper for article list endpoint responses */
export interface ArticleListResponse {
  articles: ArticleResponse[];
  articlesCount: number;
}

/** Create article request body */
export interface CreateArticleRequest {
  article: {
    title: string;
    description: string;
    body: string;
    tagList?: string[];
  };
}

/** Update article request body */
export interface UpdateArticleRequest {
  article: {
    title?: string;
    description?: string;
    body?: string;
  };
}
