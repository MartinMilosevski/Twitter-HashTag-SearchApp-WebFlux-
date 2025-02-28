package com.example.twitterpostsearch.Service.Impl;

import com.example.twitterpostsearch.Domain.MetaDataForTweets;
import com.example.twitterpostsearch.Repository.MetaDataForTweetRepository;
import com.example.twitterpostsearch.Service.MetaDataForTweetService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class MetaDataForTweetServiceImpl implements MetaDataForTweetService {

    private final MetaDataForTweetRepository metaDataForTweetRepository;

    public MetaDataForTweetServiceImpl(MetaDataForTweetRepository metaDataForTweetRepository) {
        this.metaDataForTweetRepository = metaDataForTweetRepository;
    }

    @Override
    public Flux<MetaDataForTweets> getMetaDataForTweet(String Hashtag) {
        return metaDataForTweetRepository.findAll().filter(r->r.getHashtag().equals(Hashtag));
    }
}
