CREATE TABLE IF NOT EXISTS tweets (
    tweet_id SERIAL PRIMARY KEY,
    author_id VARCHAR(255) UNIQUE NOT NULL,
    text TEXT NOT NULL,
    hashtag TEXT NOT NULL
);
