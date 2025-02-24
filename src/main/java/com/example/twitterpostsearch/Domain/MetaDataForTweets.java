package com.example.twitterpostsearch.Domain;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "meta")
public class MetaDataForTweets {

    //Id na requestot za hashtagot nadvoresen kluc na Tweet koga kje se pobaruva
    @Id
    private Long RequestId;
    private String NewestPostId;
    private String OldestPostId;
    private int CountPosts;

    public MetaDataForTweets() {}

    public MetaDataForTweets(String newestPostId, String oldestPostId, int countPosts) {
        this.NewestPostId = newestPostId;
        this.OldestPostId = oldestPostId;
        this.CountPosts = countPosts;
    }

}