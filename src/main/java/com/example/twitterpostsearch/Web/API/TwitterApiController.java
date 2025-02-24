package com.example.twitterpostsearch.Web.API;

import com.example.twitterpostsearch.Domain.MetaDataForTweets;
import com.example.twitterpostsearch.Domain.Tweet;
import com.example.twitterpostsearch.Repository.MetaDataForTweetRepository;
import com.example.twitterpostsearch.Repository.TweetRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/twitter")
public class TwitterApiController {

    public final TweetRepository tweetRepository;
    public final MetaDataForTweetRepository metaDataForTweetRepository;

    @Value("${twitter.api.url}")
    private String twitterApiUrl;

    @Value("${twitter.api.bearer-token}")
    private String bearerToken;

    private final WebClient.Builder webClientBuilder;


    public TwitterApiController(WebClient.Builder webClientBuilder, TweetRepository tweetRepository, MetaDataForTweetRepository metaDataForTweetRepository) {
        this.webClientBuilder = webClientBuilder;
        this.tweetRepository = tweetRepository;
        this.metaDataForTweetRepository = metaDataForTweetRepository;
        System.out.println("WebClient Builder Injected: " + webClientBuilder);  // This log helps check injection
        System.out.println("TweetRepository Injected: " + tweetRepository);    // This log helps check if TweetRepository is injected
    }

    @GetMapping("/search")
    public Mono<String> searchTweets(@RequestParam String hashtag) {
        System.out.println("Received request with hashtag: " + hashtag);

        int maxResults = 10;
        String url = twitterApiUrl + "?query=" + hashtag + "&max_results=" + maxResults;

        return webClientBuilder.build()
                .get()
                .uri(url)
                .header("Authorization", "Bearer " + bearerToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            System.out.println("Error response: " + errorBody);
                            return Mono.error(new RuntimeException("API request failed: " + errorBody));
                        }))
                .bodyToMono(String.class)
                .flatMap(response -> {
                    System.out.println("API response: " + response);
                    try {
                        JsonNode root = new ObjectMapper().readTree(response);
                        JsonNode tweetData = root.path("data");
                       /* JsonNode metaData = tweetData.path("meta");

                        if(metaData.isMissingNode()){
                            metaDataForTweetRepository.save(new MetaDataForTweets(
                                    metaData.path("newest_id").asText(),
                                    metaData.path("oldest_id").asText(),
                                    metaData.path("result_count").asInt()
                            ));
                        }*/

                        List<Tweet> tweetList = new ArrayList<>();
                        for (JsonNode tweets : tweetData) {
                            Tweet tweet = new Tweet();
                            if (tweets.path("text") != null && !tweets.path("text").asText().isEmpty()
                                    && tweets.path("id") != null && !tweets.path("id").asText().isEmpty()){
                                tweet.setText(tweets.path("text").asText());
                                tweet.setAuthorId(tweets.path("id").asText());
                                tweet.setHashtag(hashtag);
                                tweetList.add(tweet);
                            }else{
                                System.out.println("Skip the tweet due to missing fields");
                            }
                        }
                        System.out.println("Tweet List: "+tweetList);
                        return tweetRepository.saveAll(tweetList).collectList()
                                .then(Mono.just("Successfully retrieved " + tweetList.size() + " tweets"));
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(e));
                    }
                })
                .doOnTerminate(() -> System.out.println("Request completed"))
                .onErrorResume(e -> Mono.just("Error fetching tweets: " + e.getMessage()));
    }

}
