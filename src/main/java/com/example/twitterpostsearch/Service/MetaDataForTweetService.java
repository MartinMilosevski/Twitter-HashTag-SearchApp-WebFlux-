package com.example.twitterpostsearch.Service;

import com.example.twitterpostsearch.Domain.MetaDataForTweets;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

public interface MetaDataForTweetService {

    Mono<MetaDataForTweets> getMetaDataForTweet(Long tweetId);

}
