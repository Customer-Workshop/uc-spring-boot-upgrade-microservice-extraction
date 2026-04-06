CREATE TABLE users (
  id VARCHAR(255) PRIMARY KEY,
  username VARCHAR(255) UNIQUE,
  password VARCHAR(255),
  email VARCHAR(255) UNIQUE,
  bio TEXT,
  image VARCHAR(511)
);

CREATE TABLE follows (
  user_id VARCHAR(255) NOT NULL,
  follow_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (user_id, follow_id)
);
