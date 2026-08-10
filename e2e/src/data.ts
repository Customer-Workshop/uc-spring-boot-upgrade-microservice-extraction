let counter = 0;

function unique(prefix: string): string {
  counter += 1;
  return `${prefix}${Date.now().toString(36)}${counter}`;
}

export interface NewUser {
  username: string;
  email: string;
  password: string;
}

export function newUser(prefix = "e2euser"): NewUser {
  const username = unique(prefix);
  return { username, email: `${username}@example.com`, password: "password123" };
}

export interface NewArticle {
  title: string;
  description: string;
  body: string;
  tagList: string[];
}

export function newArticle(overrides: Partial<NewArticle> = {}): NewArticle {
  const title = `E2E article ${unique("")}`;
  return {
    title,
    description: "created by the playwright suite",
    body: `Body of ${title}\n\nWith a second paragraph.`,
    tagList: [],
    ...overrides,
  };
}

export function uniqueTag(prefix = "e2etag"): string {
  return unique(prefix);
}
