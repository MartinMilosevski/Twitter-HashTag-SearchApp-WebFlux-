package com.example.twitterpostsearch.Service;

import com.example.twitterpostsearch.Domain.MetaDataForTweets;
import reactor.core.publisher.Flux;

public interface MetaDataForTweetService {

    Flux<MetaDataForTweets> getMetaDataForTweet(String hashtag);

}
