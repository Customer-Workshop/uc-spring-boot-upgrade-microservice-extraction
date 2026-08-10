import { APIRequestContext, expect } from "@playwright/test";
import { API_URL } from "../playwright.config";
import { NewArticle, NewUser } from "./data";

export interface AuthUser extends NewUser {
  token: string;
  bio: string | null;
  image: string | null;
}

export interface ArticleResponse {
  slug: string;
  title: string;
  description: string;
  body: string;
  tagList: string[];
  favorited: boolean;
  favoritesCount: number;
  author: { username: string };
}

export interface CommentResponse {
  id: number;
  body: string;
  author: { username: string };
}

/**
 * Thin wrapper over the Conduit REST API, used by tests to arrange state
 * (users, articles, comments, follows) without driving the UI.
 */
export class ApiClient {
  constructor(private readonly request: APIRequestContext) {}

  private headers(token?: string): Record<string, string> {
    const headers: Record<string, string> = { "Content-Type": "application/json" };
    if (token) headers.Authorization = `Token ${token}`;
    return headers;
  }

  async register(user: NewUser): Promise<AuthUser> {
    const response = await this.request.post(`${API_URL}/users`, {
      headers: this.headers(),
      data: { user },
    });
    expect(response.ok(), `register ${user.username}: ${await response.text()}`).toBeTruthy();
    const body = await response.json();
    return { ...user, ...body.user };
  }

  async login(email: string, password: string): Promise<AuthUser> {
    const response = await this.request.post(`${API_URL}/users/login`, {
      headers: this.headers(),
      data: { user: { email, password } },
    });
    expect(response.ok(), `login ${email}: ${await response.text()}`).toBeTruthy();
    const body = await response.json();
    return { ...body.user, password };
  }

  async updateUser(token: string, fields: Partial<NewUser & { bio: string; image: string }>): Promise<AuthUser> {
    const response = await this.request.put(`${API_URL}/user`, {
      headers: this.headers(token),
      data: { user: fields },
    });
    expect(response.ok(), `update user: ${await response.text()}`).toBeTruthy();
    return (await response.json()).user;
  }

  async createArticle(token: string, article: NewArticle): Promise<ArticleResponse> {
    const response = await this.request.post(`${API_URL}/articles`, {
      headers: this.headers(token),
      data: { article },
    });
    expect(response.ok(), `create article: ${await response.text()}`).toBeTruthy();
    return (await response.json()).article;
  }

  async getArticle(slug: string, token?: string): Promise<ArticleResponse> {
    const response = await this.request.get(`${API_URL}/articles/${encodeURIComponent(slug)}`, {
      headers: this.headers(token),
    });
    expect(response.ok(), `get article ${slug}: ${await response.text()}`).toBeTruthy();
    return (await response.json()).article;
  }

  async deleteArticle(token: string, slug: string): Promise<void> {
    const response = await this.request.delete(`${API_URL}/articles/${encodeURIComponent(slug)}`, {
      headers: this.headers(token),
    });
    expect(response.ok(), `delete article ${slug}: ${await response.text()}`).toBeTruthy();
  }

  async createComment(token: string, slug: string, body: string): Promise<CommentResponse> {
    const response = await this.request.post(
      `${API_URL}/articles/${encodeURIComponent(slug)}/comments`,
      { headers: this.headers(token), data: { comment: { body } } }
    );
    expect(response.ok(), `create comment: ${await response.text()}`).toBeTruthy();
    return (await response.json()).comment;
  }

  async listComments(slug: string): Promise<CommentResponse[]> {
    const response = await this.request.get(
      `${API_URL}/articles/${encodeURIComponent(slug)}/comments`
    );
    expect(response.ok(), `list comments ${slug}: ${await response.text()}`).toBeTruthy();
    return (await response.json()).comments;
  }

  async favorite(token: string, slug: string): Promise<ArticleResponse> {
    const response = await this.request.post(
      `${API_URL}/articles/${encodeURIComponent(slug)}/favorite`,
      { headers: this.headers(token), data: {} }
    );
    expect(response.ok(), `favorite ${slug}: ${await response.text()}`).toBeTruthy();
    return (await response.json()).article;
  }

  async follow(token: string, username: string): Promise<void> {
    const response = await this.request.post(
      `${API_URL}/profiles/${encodeURIComponent(username)}/follow`,
      { headers: this.headers(token), data: {} }
    );
    expect(response.ok(), `follow ${username}: ${await response.text()}`).toBeTruthy();
  }

  async getProfile(username: string, token?: string): Promise<{ username: string; bio: string | null; following: boolean }> {
    const response = await this.request.get(
      `${API_URL}/profiles/${encodeURIComponent(username)}`,
      { headers: this.headers(token) }
    );
    expect(response.ok(), `get profile ${username}: ${await response.text()}`).toBeTruthy();
    return (await response.json()).profile;
  }

  async tags(): Promise<string[]> {
    const response = await this.request.get(`${API_URL}/tags`);
    expect(response.ok(), `list tags: ${await response.text()}`).toBeTruthy();
    return (await response.json()).tags;
  }
}
