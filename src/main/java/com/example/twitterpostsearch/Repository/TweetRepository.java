package com.example.twitterpostsearch.Repository;
import com.example.twitterpostsearch.Domain.Tweet;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;


@Repository
public interface TweetRepository extends ReactiveCrudRepository<Tweet, Long> {

    Flux<Tweet> findAllByHashtag(String hashtag);
}
