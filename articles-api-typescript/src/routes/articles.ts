import { Router, Request, Response } from "express";
import { DataSource } from "typeorm";
import { ArticleService } from "../services/ArticleService";
import { AuthenticatedRequest, requireAuth } from "../middleware/auth";
import { CreateArticleRequest, UpdateArticleRequest } from "../types";

export function createArticlesRouter(dataSource: DataSource): Router {
  const router = Router();
  const articleService = new ArticleService(dataSource);

  /**
   * GET /api/articles
   * List articles with optional filters: tag, author, favorited, offset, limit.
   * Mirrors Java ArticlesApi.getArticles()
   */
  router.get("/", async (req: Request, res: Response) => {
    try {
      const offset = parseInt(req.query.offset as string) || 0;
      const limit = parseInt(req.query.limit as string) || 20;
      const tag = req.query.tag as string | undefined;
      const author = req.query.author as string | undefined;
      const favorited = req.query.favorited as string | undefined;
      const currentUser = (req as AuthenticatedRequest).user;

      const { articles, count } = await articleService.listArticles(
        offset,
        limit,
        tag,
        author,
        favorited
      );

      const enriched = await articleService.enrichArticles(
        articles,
        currentUser
      );

      res.json({ articles: enriched, articlesCount: count });
    } catch (err) {
      res.status(500).json({ errors: { body: ["internal server error"] } });
    }
  });

  /**
   * GET /api/articles/feed
   * Articles from followed authors (requires auth).
   * Mirrors Java ArticlesApi.getFeed()
   */
  router.get("/feed", requireAuth, async (req: Request, res: Response) => {
    try {
      const offset = parseInt(req.query.offset as string) || 0;
      const limit = parseInt(req.query.limit as string) || 20;
      const currentUser = (req as AuthenticatedRequest).user!;

      const { articles, count } = await articleService.getFeed(
        currentUser.id,
        offset,
        limit
      );

      const enriched = await articleService.enrichArticles(
        articles,
        currentUser
      );

      res.json({ articles: enriched, articlesCount: count });
    } catch (err) {
      res.status(500).json({ errors: { body: ["internal server error"] } });
    }
  });

  /**
   * POST /api/articles
   * Create article (requires auth).
   * Mirrors Java ArticlesApi.createArticle()
   */
  router.post("/", requireAuth, async (req: Request, res: Response) => {
    try {
      const currentUser = (req as AuthenticatedRequest).user!;
      const body = req.body as CreateArticleRequest;

      if (
        !body.article ||
        !body.article.title ||
        !body.article.description ||
        !body.article.body
      ) {
        res.status(422).json({
          errors: { body: ["title, description, and body are required"] },
        });
        return;
      }

      const article = await articleService.createArticle(
        body.article.title,
        body.article.description,
        body.article.body,
        body.article.tagList || [],
        currentUser.id
      );

      const enriched = await articleService.enrichArticle(
        article,
        currentUser
      );

      res.json({ article: enriched });
    } catch (err) {
      res.status(500).json({ errors: { body: ["internal server error"] } });
    }
  });

  /**
   * GET /api/articles/:slug
   * Get single article by slug.
   * Mirrors Java ArticleApi.article()
   */
  router.get("/:slug", async (req: Request, res: Response) => {
    try {
      const currentUser = (req as AuthenticatedRequest).user;
      const article = await articleService.findBySlug(req.params.slug);

      if (!article) {
        res.status(404).json({ errors: { body: ["article not found"] } });
        return;
      }

      const enriched = await articleService.enrichArticle(
        article,
        currentUser
      );

      res.json({ article: enriched });
    } catch (err) {
      res.status(500).json({ errors: { body: ["internal server error"] } });
    }
  });

  /**
   * PUT /api/articles/:slug
   * Update article (requires auth, author only).
   * Mirrors Java ArticleApi.updateArticle()
   */
  router.put("/:slug", requireAuth, async (req: Request, res: Response) => {
    try {
      const currentUser = (req as AuthenticatedRequest).user!;
      const article = await articleService.findBySlug(req.params.slug);

      if (!article) {
        res.status(404).json({ errors: { body: ["article not found"] } });
        return;
      }

      if (article.userId !== currentUser.id) {
        res.status(403).json({ errors: { body: ["not authorized"] } });
        return;
      }

      const body = req.body as UpdateArticleRequest;
      const updated = await articleService.updateArticle(
        article,
        body.article?.title,
        body.article?.description,
        body.article?.body
      );

      const enriched = await articleService.enrichArticle(
        updated,
        currentUser
      );

      res.json({ article: enriched });
    } catch (err) {
      res.status(500).json({ errors: { body: ["internal server error"] } });
    }
  });

  /**
   * DELETE /api/articles/:slug
   * Delete article (requires auth, author only).
   * Mirrors Java ArticleApi.deleteArticle()
   */
  router.delete(
    "/:slug",
    requireAuth,
    async (req: Request, res: Response) => {
      try {
        const currentUser = (req as AuthenticatedRequest).user!;
        const article = await articleService.findBySlug(req.params.slug);

        if (!article) {
          res.status(404).json({ errors: { body: ["article not found"] } });
          return;
        }

        if (article.userId !== currentUser.id) {
          res.status(403).json({ errors: { body: ["not authorized"] } });
          return;
        }

        await articleService.deleteArticle(article);
        res.status(204).send();
      } catch (err) {
        res.status(500).json({ errors: { body: ["internal server error"] } });
      }
    }
  );

  return router;
}
