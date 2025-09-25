package com.example.twitterpostsearch.Service;

import com.example.twitterpostsearch.Domain.Tweet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface TweetService
{
    Flux<Tweet> getTweets(String Hashtag);


    Mono<Tweet> getTweet();

    Mono<Boolean> getHashtag(String Hashtag);

    Flux<Tweet> fetchandSaveTweets(String Hashtag);

    Flux<Tweet> streamTweets();

    Flux<Tweet> streamTweetsByHashtag(String Hashtag);
}