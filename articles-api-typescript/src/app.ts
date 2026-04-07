import express from "express";
import { DataSource } from "typeorm";
import { authMiddleware } from "./middleware/auth";
import { createArticlesRouter } from "./routes/articles";

export function createApp(dataSource: DataSource): express.Application {
  const app = express();

  app.use(express.json());

  // Auth stub middleware — attaches first user from DB to all requests
  app.use(authMiddleware(dataSource));

  // Mount articles routes under /api/articles
  app.use("/api/articles", createArticlesRouter(dataSource));

  return app;
}
