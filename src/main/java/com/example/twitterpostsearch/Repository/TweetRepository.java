package com.example.twitterpostsearch.Repository;
import org.springframework.context.annotation.Bean;
import com.example.twitterpostsearch.Domain.Tweet;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TweetRepository extends ReactiveCrudRepository<Tweet, Long> {
}
