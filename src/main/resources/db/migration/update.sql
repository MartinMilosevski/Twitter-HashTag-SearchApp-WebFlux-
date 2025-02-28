ALTER TABLE tweets
    ADD CONSTRAINT unique_author_id UNIQUE (author_id);