package com.example.twitterpostsearch.Service.Impl;
import org.springframework.context.annotation.Bean;
import com.example.twitterpostsearch.Domain.Tweet;
import com.example.twitterpostsearch.Repository.TweetRepository;
import com.example.twitterpostsearch.Service.TweetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class TweetServiceImpl implements TweetService {

    public final TweetRepository tweetRepository;

    @Autowired
    public TweetServiceImpl(TweetRepository tweetRepository) {
        this.tweetRepository = tweetRepository;
    }

    @Override
    public Flux<Tweet> getTweets() {
        return tweetRepository.findAll();
    }

}
