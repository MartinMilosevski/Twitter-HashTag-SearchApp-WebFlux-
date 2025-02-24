package com.example.twitterpostsearch.Repository;
import com.example.twitterpostsearch.Domain.MetaDataForTweets;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetaDataForTweetRepository extends ReactiveCrudRepository<MetaDataForTweets,Long> {
}
