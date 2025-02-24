CREATE TABLE IF NOT EXISTS tweets (
        request_id INT PRIMARY KEY,
        newest_id VARCHAR(255) NOT NULL,
        oldets_id VARCHAR(255) NOT NULL,
        count INT NOT NULL
);