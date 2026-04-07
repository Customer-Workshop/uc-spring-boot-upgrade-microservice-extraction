import { DataSource, In } from "typeorm";
import { Article } from "../entities/Article";
import { Tag } from "../entities/Tag";
import { User } from "../entities/User";
import { FollowRelation } from "../entities/FollowRelation";
import { ArticleFavorite } from "../entities/ArticleFavorite";
import { ArticleResponse, ProfileResponse } from "../types";

export class ArticleService {
  constructor(private dataSource: DataSource) {}

  /**
   * Build a ProfileResponse (matching Java's ProfileData with @JsonIgnore on id).
   */
  private buildProfile(user: User, following: boolean): ProfileResponse {
    return {
      username: user.username,
      bio: user.bio || null,
      image: user.image || null,
      following,
    };
  }

  /**
   * Build an ArticleResponse matching the Java ArticleData shape.
   * tagList is sorted alphabetically.
   */
  private buildArticleResponse(
    article: Article,
    author: User,
    favorited: boolean,
    favoritesCount: number,
    followingAuthor: boolean
  ): ArticleResponse {
    const tagList = article.tags
      .map((t) => t.name)
      .sort((a, b) => a.localeCompare(b));

    return {
      id: article.id,
      slug: article.slug,
      title: article.title,
      description: article.description,
      body: article.body,
      favorited,
      favoritesCount,
      createdAt: article.createdAt.toISOString(),
      updatedAt: article.updatedAt.toISOString(),
      tagList,
      author: this.buildProfile(author, followingAuthor),
    };
  }

  /**
   * Check if currentUser is following the author.
   */
  private async isFollowing(
    currentUserId: string | undefined,
    targetId: string
  ): Promise<boolean> {
    if (!currentUserId) return false;
    const repo = this.dataSource.getRepository(FollowRelation);
    const relation = await repo.findOne({
      where: { userId: currentUserId, targetId },
    });
    return relation !== null;
  }

  /**
   * Check if currentUser has favorited the article.
   */
  private async isFavorited(
    currentUserId: string | undefined,
    articleId: string
  ): Promise<boolean> {
    if (!currentUserId) return false;
    const repo = this.dataSource.getRepository(ArticleFavorite);
    const fav = await repo.findOne({
      where: { userId: currentUserId, articleId },
    });
    return fav !== null;
  }

  /**
   * Get the favorites count for an article.
   */
  private async getFavoritesCount(articleId: string): Promise<number> {
    const repo = this.dataSource.getRepository(ArticleFavorite);
    return repo.count({ where: { articleId } });
  }

  /**
   * Enrich a single article with author, favorited status, and favorites count.
   */
  async enrichArticle(
    article: Article,
    currentUser?: User
  ): Promise<ArticleResponse> {
    const userRepo = this.dataSource.getRepository(User);
    const author = await userRepo.findOneOrFail({ where: { id: article.userId } });
    const favorited = await this.isFavorited(currentUser?.id, article.id);
    const favoritesCount = await this.getFavoritesCount(article.id);
    const following = await this.isFollowing(currentUser?.id, author.id);

    return this.buildArticleResponse(
      article,
      author,
      favorited,
      favoritesCount,
      following
    );
  }

  /**
   * Enrich a list of articles.
   */
  async enrichArticles(
    articles: Article[],
    currentUser?: User
  ): Promise<ArticleResponse[]> {
    return Promise.all(
      articles.map((article) => this.enrichArticle(article, currentUser))
    );
  }

  /**
   * Find article by slug.
   */
  async findBySlug(slug: string): Promise<Article | null> {
    const repo = this.dataSource.getRepository(Article);
    return repo.findOne({ where: { slug }, relations: ["tags"] });
  }

  /**
   * Find article by id.
   */
  async findById(id: string): Promise<Article | null> {
    const repo = this.dataSource.getRepository(Article);
    return repo.findOne({ where: { id }, relations: ["tags"] });
  }

  /**
   * Create a new article.
   */
  async createArticle(
    title: string,
    description: string,
    body: string,
    tagList: string[],
    userId: string
  ): Promise<Article> {
    const repo = this.dataSource.getRepository(Article);
    const tagRepo = this.dataSource.getRepository(Tag);

    // Deduplicate tags
    const uniqueTagNames = [...new Set(tagList)];
    const tags: Tag[] = [];
    for (const name of uniqueTagNames) {
      let tag = await tagRepo.findOne({ where: { name } });
      if (!tag) {
        tag = new Tag(name);
        await tagRepo.save(tag);
      }
      tags.push(tag);
    }

    const article = new Article(title, description, body, [], userId);
    article.tags = tags;
    await repo.save(article);
    return repo.findOneOrFail({ where: { id: article.id }, relations: ["tags"] });
  }

  /**
   * Update an article (only non-empty fields).
   * Mirrors Java Article.update() which uses Util.isEmpty().
   */
  async updateArticle(
    article: Article,
    title?: string,
    description?: string,
    body?: string
  ): Promise<Article> {
    const repo = this.dataSource.getRepository(Article);
    article.update(title, description, body);
    await repo.save(article);
    return repo.findOneOrFail({ where: { id: article.id }, relations: ["tags"] });
  }

  /**
   * Delete an article.
   */
  async deleteArticle(article: Article): Promise<void> {
    const repo = this.dataSource.getRepository(Article);
    await repo.remove(article);
  }

  /**
   * List articles with optional filters: tag, author, favorited.
   * Mirrors Java ArticleQueryService.findRecentArticles().
   */
  async listArticles(
    offset: number,
    limit: number,
    tag?: string,
    author?: string,
    favoritedBy?: string
  ): Promise<{ articles: Article[]; count: number }> {
    const repo = this.dataSource.getRepository(Article);
    const userRepo = this.dataSource.getRepository(User);

    let qb = repo
      .createQueryBuilder("article")
      .leftJoinAndSelect("article.tags", "tag");

    if (tag) {
      qb = qb
        .innerJoin("article.tags", "filterTag", "filterTag.name = :tagName", {
          tagName: tag,
        });
    }

    if (author) {
      const authorUser = await userRepo.findOne({
        where: { username: author },
      });
      if (authorUser) {
        qb = qb.andWhere("article.userId = :authorId", {
          authorId: authorUser.id,
        });
      } else {
        return { articles: [], count: 0 };
      }
    }

    if (favoritedBy) {
      const favUser = await userRepo.findOne({
        where: { username: favoritedBy },
      });
      if (favUser) {
        const favRepo = this.dataSource.getRepository(ArticleFavorite);
        const favs = await favRepo.find({ where: { userId: favUser.id } });
        const favArticleIds = favs.map((f) => f.articleId);
        if (favArticleIds.length === 0) {
          return { articles: [], count: 0 };
        }
        qb = qb.andWhere("article.id IN (:...favArticleIds)", {
          favArticleIds,
        });
      } else {
        return { articles: [], count: 0 };
      }
    }

    const count = await qb.getCount();

    const articles = await qb
      .orderBy("article.createdAt", "DESC")
      .skip(offset)
      .take(limit)
      .getMany();

    return { articles, count };
  }

  /**
   * Get feed articles — articles from users that currentUser follows.
   * Mirrors Java ArticleQueryService.findUserFeed().
   */
  async getFeed(
    userId: string,
    offset: number,
    limit: number
  ): Promise<{ articles: Article[]; count: number }> {
    const followRepo = this.dataSource.getRepository(FollowRelation);
    const articleRepo = this.dataSource.getRepository(Article);

    const followedRelations = await followRepo.find({
      where: { userId },
    });
    const followedUserIds = followedRelations.map((r) => r.targetId);

    if (followedUserIds.length === 0) {
      return { articles: [], count: 0 };
    }

    const count = await articleRepo.count({
      where: { userId: In(followedUserIds) },
    });

    const articles = await articleRepo.find({
      where: { userId: In(followedUserIds) },
      relations: ["tags"],
      order: { createdAt: "DESC" },
      skip: offset,
      take: limit,
    });

    return { articles, count };
  }
}
