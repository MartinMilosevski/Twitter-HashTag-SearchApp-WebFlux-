package com.example.twitterpostsearch.Service;

import com.example.twitterpostsearch.Domain.Tweet;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

public interface TweetService
{
    Flux<Tweet> getTweets();
}