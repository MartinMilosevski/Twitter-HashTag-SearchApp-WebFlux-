package com.example.twitterpostsearch.Web.Frontend;

import com.example.twitterpostsearch.Domain.Tweet;
import com.example.twitterpostsearch.Service.TweetService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/searchTweets")
@CrossOrigin("http://localhost:3000")
public class TweetsToFrontendController {

    private final TweetService tweetService;


    public TweetsToFrontendController(TweetService tweetService) {
        this.tweetService = tweetService;
    }

    @GetMapping("/searchtweet")
    public Flux<Tweet> getAllTweetsFromHashtag(@RequestParam String hashtag) {
        return tweetService.getHashtag(hashtag)
                .flatMapMany(exists->{
                    if (exists){
                        return tweetService.getTweets(hashtag);
                    }
                    return tweetService.fetchandSaveTweets(hashtag);
                });
    }

}
