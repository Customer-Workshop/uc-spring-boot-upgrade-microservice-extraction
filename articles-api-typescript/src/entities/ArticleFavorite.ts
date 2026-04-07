import "reflect-metadata";
import { Entity, PrimaryColumn } from "typeorm";

@Entity("article_favorites")
export class ArticleFavorite {
  @PrimaryColumn("varchar")
  articleId: string;

  @PrimaryColumn("varchar")
  userId: string;

  constructor(articleId: string, userId: string) {
    this.articleId = articleId ?? "";
    this.userId = userId ?? "";
  }
}
