package com.example.twitterpostsearch.Domain;
import jakarta.annotation.Generated;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name="Tweets")
public class Tweet {

    public Long getTweetId() {
        return TweetId;
    }

    public void setTweetId(Long tweetId) {
        TweetId = tweetId;
    }

    public String getAuthorId() {
        return AuthorId;
    }

    public void setAuthorId(String authorId) {
        AuthorId = authorId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Id
    private Long TweetId;


    public String AuthorId;
    public String text;

    public Tweet(String authorId, String text) {
        AuthorId = authorId;
        this.text = text;
    }

    public Tweet(){}

}
