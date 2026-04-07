import "reflect-metadata";
import { getDataSource } from "./database";
import { createApp } from "./app";

const PORT = process.env.PORT || 3000;

async function main() {
  const dataSource = await getDataSource("articles.sqlite");
  const app = createApp(dataSource);

  app.listen(PORT, () => {
    console.log(`Articles API running on http://localhost:${PORT}`);
  });
}

main().catch((err) => {
  console.error("Failed to start server:", err);
  process.exit(1);
});
