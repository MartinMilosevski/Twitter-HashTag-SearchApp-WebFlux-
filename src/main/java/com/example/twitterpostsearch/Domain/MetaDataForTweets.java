package com.example.twitterpostsearch.Domain;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "meta_tweets")
public class MetaDataForTweets {

    //Id na requestot za hashtagot nadvoresen kluc na Tweet koga kje se pobaruva
    @Id
    private Long RequestId;

    private String newestpostid;


    private String oldestpostid;


    private int countposts;


    private String hashtag;


    public MetaDataForTweets(String newestpostid, String oldestpostid, int countposts, String hashtag) {
        this.newestpostid = newestpostid;
        this.oldestpostid = oldestpostid;
        this.countposts = countposts;
        this.hashtag = hashtag;
    }


    public String getNewestpostid() {
        return newestpostid;
    }

    public void setNewestpostid(String newestpostid) {
        this.newestpostid = newestpostid;
    }

    public String getOldestpostid() {
        return oldestpostid;
    }

    public void setOldestpostid(String oldestpostid) {
        this.oldestpostid = oldestpostid;
    }

    public int getCountposts() {
        return countposts;
    }

    public void setCountposts(int countposts) {
        this.countposts = countposts;
    }

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }
}