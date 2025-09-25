package com.example.twitterpostsearch.Web.Frontend;

import com.example.twitterpostsearch.Domain.MetaDataForTweets;
import com.example.twitterpostsearch.Domain.Tweet;
import com.example.twitterpostsearch.Service.MetaDataForTweetService;
import com.example.twitterpostsearch.Service.TweetService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/searchTweets")
@CrossOrigin("http://localhost:3000")
public class TweetsToFrontendController {

    private final TweetService tweetService;
    private final MetaDataForTweetService metaDataForTweetService;



    public TweetsToFrontendController(TweetService tweetService
            , MetaDataForTweetService metaDataForTweetService) {
        this.tweetService = tweetService;
        this.metaDataForTweetService = metaDataForTweetService;

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

    @GetMapping("/metaDataForTweet")
    public Flux<MetaDataForTweets> getMetaDataForTweetHashtag(@RequestParam String hashtag) {
        return  metaDataForTweetService.getMetaDataForTweet(hashtag);
     }


    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> streamTweetsByHashtag(@RequestParam String hashtag) {
        return tweetService.streamTweetsByHashtag(hashtag);
    }

}
