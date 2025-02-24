package com.example.twitterpostsearch.Service.Impl;
import com.example.twitterpostsearch.Web.API.TwitterApiController;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import com.example.twitterpostsearch.Domain.Tweet;
import com.example.twitterpostsearch.Repository.TweetRepository;
import com.example.twitterpostsearch.Service.TweetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TweetServiceImpl implements TweetService {

    public final TweetRepository tweetRepository;
    public final TwitterApiController twitterApiController;
    private final WebClient.Builder webClientBuilder;

    @Autowired
    public TweetServiceImpl(TweetRepository tweetRepository, TwitterApiController twitterApiController, @Qualifier("webClientBuilder") WebClient.Builder webClientBuilder) {
        this.tweetRepository = tweetRepository;
        this.twitterApiController = twitterApiController;
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Flux<Tweet> getTweets(String Hashtag) {
        return tweetRepository.findAll().filter(tweet -> tweet.getHashtag().equals(Hashtag));
    }

    @Override
    public Mono<Tweet> getTweet() {
        return null;
    }

    @Override
    public Mono<Boolean> getHashtag(String Hashtag) {
        return tweetRepository
                .findAll()
                .filter(tweet -> tweet.getHashtag().equals(Hashtag))
                .hasElements()
                .defaultIfEmpty(false);
    }

    @Override
    public Flux<Tweet> fetchandSaveTweets(String Hashtag) {
        String url = "http://localhost:8080/twitter/search?hashtag=" + Hashtag;
        System.out.println("Calling URL: " + url);

        // Правиме повик преку WebClient
        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(String.class)
                .flatMap(response -> {
                    System.out.println("Response from search: " + response);
                    return getTweets(Hashtag);
                })
                .doOnError(e -> System.out.println("Error during fetch: " + e.getMessage()));
    }

}
