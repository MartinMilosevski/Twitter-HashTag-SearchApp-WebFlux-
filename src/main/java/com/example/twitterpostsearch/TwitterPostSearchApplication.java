package com.example.twitterpostsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:8080")
@SpringBootApplication
@EnableR2dbcRepositories
/*@ComponentScan(basePackages = "com.example.twitterpostsearch.Repository")*/
public class TwitterPostSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(TwitterPostSearchApplication.class, args);
    }

}
