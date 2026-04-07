import "reflect-metadata";
import { Entity, PrimaryColumn, Column, ManyToMany, JoinTable } from "typeorm";
import { v4 as uuidv4 } from "uuid";
import { Tag } from "./Tag";

/**
 * Converts a title to a URL-friendly slug.
 * Mirrors the Java Article.toSlug() regex:
 *   title.toLowerCase().replaceAll("[\\&|[\\uFE30-\\uFFA0]|\\'|\\"|\\s\\?\\,\\.]+", "-")
 */
export function toSlug(title: string): string {
  return title
    .toLowerCase()
    .replace(/[&|\uFE30-\uFFA0'"\s?,.[\]]+/g, "-");
}

@Entity("articles")
export class Article {
  @PrimaryColumn("varchar")
  id: string;

  @Column("varchar")
  slug: string;

  @Column("varchar")
  title: string;

  @Column("varchar")
  description: string;

  @Column("text")
  body: string;

  @Column("varchar")
  userId: string;

  @Column("datetime")
  createdAt: Date;

  @Column("datetime")
  updatedAt: Date;

  @ManyToMany(() => Tag, { cascade: true, eager: true })
  @JoinTable({
    name: "article_tags",
    joinColumn: { name: "articleId", referencedColumnName: "id" },
    inverseJoinColumn: { name: "tagId", referencedColumnName: "id" },
  })
  tags!: Tag[];

  constructor(
    title: string,
    description: string,
    body: string,
    tagList: string[],
    userId: string
  ) {
    this.id = uuidv4();
    this.title = title ?? "";
    this.slug = toSlug(this.title);
    this.description = description ?? "";
    this.body = body ?? "";
    this.userId = userId ?? "";
    const now = new Date();
    this.createdAt = now;
    this.updatedAt = now;
  }

  update(title?: string, description?: string, body?: string): void {
    if (title && title.length > 0) {
      this.title = title;
      this.slug = toSlug(title);
      this.updatedAt = new Date();
    }
    if (description && description.length > 0) {
      this.description = description;
      this.updatedAt = new Date();
    }
    if (body && body.length > 0) {
      this.body = body;
      this.updatedAt = new Date();
    }
  }
}
