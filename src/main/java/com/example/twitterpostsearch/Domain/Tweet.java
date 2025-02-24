package com.example.twitterpostsearch.Domain;
import jakarta.annotation.Generated;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Table(name="tweets")
public class Tweet {

    public Long getTweetId() {
        return tweetId;
    }

    public void setTweetId(Long tweetId) {
        this.tweetId = tweetId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Id
    private Long tweetId;

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public String authorId;
    public String text;
    public String hashtag;

    public Tweet(String authorId, String text,String hashtag) {
        this.hashtag=hashtag;
        this.authorId = authorId;
        this.text = text;
    }

    

    public Tweet(){}

}
