import "reflect-metadata";
import request from "supertest";
import { DataSource } from "typeorm";
import { createApp } from "../src/app";
import { createDataSource } from "../src/database";
import { User } from "../src/entities/User";
import { Article } from "../src/entities/Article";
import { Tag } from "../src/entities/Tag";
import { FollowRelation } from "../src/entities/FollowRelation";
import { ArticleFavorite } from "../src/entities/ArticleFavorite";
import express from "express";

let dataSource: DataSource;
let app: express.Application;
let testUser: User;
let otherUser: User;

beforeAll(async () => {
  dataSource = createDataSource(":memory:");
  await dataSource.initialize();
  app = createApp(dataSource);

  // Seed test users
  const userRepo = dataSource.getRepository(User);
  testUser = new User("alice@test.com", "alice", "password123", "A short bio", "https://example.com/alice.jpg");
  otherUser = new User("bob@test.com", "bob", "password456", "Bob bio", "https://example.com/bob.jpg");
  await userRepo.save([testUser, otherUser]);
});

afterAll(async () => {
  if (dataSource && dataSource.isInitialized) {
    await dataSource.destroy();
  }
});

describe("Articles API — JSON response shapes", () => {
  describe("POST /api/articles", () => {
    it("should create an article and return the correct single-article shape", async () => {
      const res = await request(app)
        .post("/api/articles")
        .send({
          article: {
            title: "How to train your dragon",
            description: "Ever wonder how?",
            body: "You have to believe",
            tagList: ["reactjs", "angularjs", "dragons"],
          },
        })
        .expect(200);

      // Verify top-level wrapper
      expect(res.body).toHaveProperty("article");
      const article = res.body.article;

      // Verify all required fields exist
      expect(article).toHaveProperty("id");
      expect(article).toHaveProperty("slug");
      expect(article).toHaveProperty("title", "How to train your dragon");
      expect(article).toHaveProperty("description", "Ever wonder how?");
      expect(article).toHaveProperty("body", "You have to believe");
      expect(article).toHaveProperty("favorited", false);
      expect(article).toHaveProperty("favoritesCount", 0);
      expect(article).toHaveProperty("createdAt");
      expect(article).toHaveProperty("updatedAt");
      expect(article).toHaveProperty("tagList");
      expect(article).toHaveProperty("author");

      // Verify tagList is sorted alphabetically
      expect(article.tagList).toEqual(["angularjs", "dragons", "reactjs"]);

      // Verify slug format (lowercase, hyphens)
      expect(article.slug).toBe("how-to-train-your-dragon");

      // Verify ISO 8601 date format
      expect(new Date(article.createdAt).toISOString()).toBe(article.createdAt);
      expect(new Date(article.updatedAt).toISOString()).toBe(article.updatedAt);

      // Verify author shape (ProfileData with id excluded via @JsonIgnore)
      expect(article.author).toHaveProperty("username");
      expect(article.author).toHaveProperty("bio");
      expect(article.author).toHaveProperty("image");
      expect(article.author).toHaveProperty("following");
      expect(article.author).not.toHaveProperty("id"); // Java @JsonIgnore on ProfileData.id

      // Verify author values
      expect(article.author.following).toBe(false);
    });

    it("should deduplicate tags", async () => {
      const res = await request(app)
        .post("/api/articles")
        .send({
          article: {
            title: "Duplicate Tags Test",
            description: "Testing dedup",
            body: "Body text",
            tagList: ["tag1", "tag1", "tag2", "tag2", "tag3"],
          },
        })
        .expect(200);

      expect(res.body.article.tagList).toEqual(["tag1", "tag2", "tag3"]);
    });

    it("should return 422 for missing required fields", async () => {
      await request(app)
        .post("/api/articles")
        .send({
          article: {
            title: "",
            description: "",
            body: "",
          },
        })
        .expect(422);
    });
  });

  describe("GET /api/articles/:slug", () => {
    it("should return a single article with correct shape", async () => {
      // First create an article
      const createRes = await request(app)
        .post("/api/articles")
        .send({
          article: {
            title: "Article for Get Test",
            description: "Test desc",
            body: "Test body",
            tagList: ["test"],
          },
        })
        .expect(200);

      const slug = createRes.body.article.slug;

      const res = await request(app).get(`/api/articles/${slug}`).expect(200);

      // Verify single article wrapper
      expect(res.body).toHaveProperty("article");
      const article = res.body.article;

      // Verify all fields from the Java ArticleData
      expect(typeof article.id).toBe("string");
      expect(typeof article.slug).toBe("string");
      expect(typeof article.title).toBe("string");
      expect(typeof article.description).toBe("string");
      expect(typeof article.body).toBe("string");
      expect(typeof article.favorited).toBe("boolean");
      expect(typeof article.favoritesCount).toBe("number");
      expect(typeof article.createdAt).toBe("string");
      expect(typeof article.updatedAt).toBe("string");
      expect(Array.isArray(article.tagList)).toBe(true);
      expect(typeof article.author).toBe("object");

      // Author shape check
      expect(typeof article.author.username).toBe("string");
      expect(article.author).not.toHaveProperty("id");
    });

    it("should return 404 for non-existent article", async () => {
      const res = await request(app)
        .get("/api/articles/non-existent-slug")
        .expect(404);

      expect(res.body).toHaveProperty("errors");
    });
  });

  describe("GET /api/articles", () => {
    it("should return article list with correct shape", async () => {
      const res = await request(app).get("/api/articles").expect(200);

      // Verify list wrapper shape
      expect(res.body).toHaveProperty("articles");
      expect(res.body).toHaveProperty("articlesCount");
      expect(Array.isArray(res.body.articles)).toBe(true);
      expect(typeof res.body.articlesCount).toBe("number");

      // Verify each article in the list has the correct shape
      for (const article of res.body.articles) {
        expect(article).toHaveProperty("id");
        expect(article).toHaveProperty("slug");
        expect(article).toHaveProperty("title");
        expect(article).toHaveProperty("description");
        expect(article).toHaveProperty("body");
        expect(article).toHaveProperty("favorited");
        expect(article).toHaveProperty("favoritesCount");
        expect(article).toHaveProperty("createdAt");
        expect(article).toHaveProperty("updatedAt");
        expect(article).toHaveProperty("tagList");
        expect(article).toHaveProperty("author");
        expect(article.author).not.toHaveProperty("id");
      }
    });

    it("should support offset and limit query params", async () => {
      const res = await request(app)
        .get("/api/articles?offset=0&limit=1")
        .expect(200);

      expect(res.body.articles.length).toBeLessThanOrEqual(1);
      expect(typeof res.body.articlesCount).toBe("number");
    });

    it("should filter by tag", async () => {
      const res = await request(app)
        .get("/api/articles?tag=reactjs")
        .expect(200);

      expect(res.body).toHaveProperty("articles");
      expect(res.body).toHaveProperty("articlesCount");

      for (const article of res.body.articles) {
        expect(article.tagList).toContain("reactjs");
      }
    });

    it("should filter by author", async () => {
      const res = await request(app)
        .get("/api/articles?author=alice")
        .expect(200);

      expect(res.body).toHaveProperty("articles");
      for (const article of res.body.articles) {
        expect(article.author.username).toBe("alice");
      }
    });

    it("should return empty list for non-existent author", async () => {
      const res = await request(app)
        .get("/api/articles?author=nonexistent")
        .expect(200);

      expect(res.body.articles).toEqual([]);
      expect(res.body.articlesCount).toBe(0);
    });
  });

  describe("GET /api/articles/feed", () => {
    it("should return feed with correct list shape", async () => {
      const res = await request(app).get("/api/articles/feed").expect(200);

      expect(res.body).toHaveProperty("articles");
      expect(res.body).toHaveProperty("articlesCount");
      expect(Array.isArray(res.body.articles)).toBe(true);
      expect(typeof res.body.articlesCount).toBe("number");
    });

    it("should return articles from followed users", async () => {
      // Create follow relation: testUser follows otherUser
      const followRepo = dataSource.getRepository(FollowRelation);
      const follow = new FollowRelation(testUser.id, otherUser.id);
      await followRepo.save(follow);

      // Create an article by otherUser
      const articleRepo = dataSource.getRepository(Article);
      const tagRepo = dataSource.getRepository(Tag);
      const tag = new Tag("feed-tag");
      await tagRepo.save(tag);
      const article = new Article("Feed Article", "Feed desc", "Feed body", [], otherUser.id);
      article.tags = [tag];
      await articleRepo.save(article);

      const res = await request(app).get("/api/articles/feed").expect(200);

      expect(res.body.articlesCount).toBeGreaterThan(0);
      const feedArticle = res.body.articles.find(
        (a: { title: string }) => a.title === "Feed Article"
      );
      expect(feedArticle).toBeDefined();
      expect(feedArticle.author.username).toBe("bob");
      expect(feedArticle.author.following).toBe(true);
      expect(feedArticle.author).not.toHaveProperty("id");
    });
  });

  describe("PUT /api/articles/:slug", () => {
    it("should update article and return correct shape", async () => {
      // Create an article first
      const createRes = await request(app)
        .post("/api/articles")
        .send({
          article: {
            title: "Original Title",
            description: "Original desc",
            body: "Original body",
            tagList: ["update-test"],
          },
        })
        .expect(200);

      const slug = createRes.body.article.slug;

      const res = await request(app)
        .put(`/api/articles/${slug}`)
        .send({
          article: {
            title: "Updated Title",
            description: "Updated desc",
            body: "Updated body",
          },
        })
        .expect(200);

      expect(res.body).toHaveProperty("article");
      const article = res.body.article;
      expect(article.title).toBe("Updated Title");
      expect(article.description).toBe("Updated desc");
      expect(article.body).toBe("Updated body");
      expect(article.slug).toBe("updated-title");
      expect(article.author).not.toHaveProperty("id");
    });

    it("should only update non-empty fields", async () => {
      const createRes = await request(app)
        .post("/api/articles")
        .send({
          article: {
            title: "Partial Update Test",
            description: "Original desc",
            body: "Original body",
            tagList: [],
          },
        })
        .expect(200);

      const slug = createRes.body.article.slug;

      const res = await request(app)
        .put(`/api/articles/${slug}`)
        .send({
          article: {
            title: "New Title Only",
          },
        })
        .expect(200);

      expect(res.body.article.title).toBe("New Title Only");
      expect(res.body.article.description).toBe("Original desc");
      expect(res.body.article.body).toBe("Original body");
    });

    it("should return 404 for non-existent article", async () => {
      await request(app)
        .put("/api/articles/non-existent-slug")
        .send({ article: { title: "test" } })
        .expect(404);
    });

    it("should return 403 for non-author", async () => {
      // Create article by otherUser directly in DB
      const articleRepo = dataSource.getRepository(Article);
      const article = new Article(
        "Other User Article",
        "desc",
        "body",
        [],
        otherUser.id
      );
      article.tags = [];
      await articleRepo.save(article);

      await request(app)
        .put(`/api/articles/${article.slug}`)
        .send({ article: { title: "Hijacked" } })
        .expect(403);
    });
  });

  describe("DELETE /api/articles/:slug", () => {
    it("should delete article and return 204", async () => {
      const createRes = await request(app)
        .post("/api/articles")
        .send({
          article: {
            title: "To Be Deleted",
            description: "Will be removed",
            body: "Goodbye",
            tagList: ["delete-test"],
          },
        })
        .expect(200);

      const slug = createRes.body.article.slug;

      await request(app).delete(`/api/articles/${slug}`).expect(204);

      // Verify it's gone
      await request(app).get(`/api/articles/${slug}`).expect(404);
    });

    it("should return 404 for non-existent article", async () => {
      await request(app)
        .delete("/api/articles/non-existent-slug")
        .expect(404);
    });

    it("should return 403 for non-author", async () => {
      const articleRepo = dataSource.getRepository(Article);
      const article = new Article(
        "Cannot Delete This",
        "desc",
        "body",
        [],
        otherUser.id
      );
      article.tags = [];
      await articleRepo.save(article);

      await request(app)
        .delete(`/api/articles/${article.slug}`)
        .expect(403);
    });
  });

  describe("Favorited and favorites count", () => {
    it("should reflect favorited status and count in response", async () => {
      // Create article
      const createRes = await request(app)
        .post("/api/articles")
        .send({
          article: {
            title: "Favorites Test Article",
            description: "Fav desc",
            body: "Fav body",
            tagList: [],
          },
        })
        .expect(200);

      const articleId = createRes.body.article.id;
      const slug = createRes.body.article.slug;

      // Add a favorite directly in DB
      const favRepo = dataSource.getRepository(ArticleFavorite);
      const fav = new ArticleFavorite(articleId, testUser.id);
      await favRepo.save(fav);

      const res = await request(app)
        .get(`/api/articles/${slug}`)
        .expect(200);

      expect(res.body.article.favorited).toBe(true);
      expect(res.body.article.favoritesCount).toBe(1);
    });
  });
});
