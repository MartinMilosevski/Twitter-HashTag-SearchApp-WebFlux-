CREATE TABLE IF NOT EXISTS meta_tweets (
        request_id SERIAL PRIMARY KEY,
        newestpostid VARCHAR(255) NOT NULL,
        oldestpostid VARCHAR(255) NOT NULL,
        countposts INT NOT NULL,
        hashtag TEXT NOT NULL
);