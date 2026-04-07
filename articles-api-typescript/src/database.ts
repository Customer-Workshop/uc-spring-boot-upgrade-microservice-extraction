import "reflect-metadata";
import { DataSource } from "typeorm";
import { Article } from "./entities/Article";
import { Tag } from "./entities/Tag";
import { User } from "./entities/User";
import { FollowRelation } from "./entities/FollowRelation";
import { ArticleFavorite } from "./entities/ArticleFavorite";

let dataSource: DataSource | null = null;

export function createDataSource(database?: string): DataSource {
  return new DataSource({
    type: "better-sqlite3",
    database: database ?? ":memory:",
    synchronize: true,
    logging: false,
    entities: [Article, Tag, User, FollowRelation, ArticleFavorite],
  });
}

export async function getDataSource(database?: string): Promise<DataSource> {
  if (!dataSource || !dataSource.isInitialized) {
    dataSource = createDataSource(database);
    await dataSource.initialize();
  }
  return dataSource;
}

export async function closeDataSource(): Promise<void> {
  if (dataSource && dataSource.isInitialized) {
    await dataSource.destroy();
    dataSource = null;
  }
}
