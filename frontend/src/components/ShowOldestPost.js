import {CardGroup, Col, Row} from "react-bootstrap";
import Card from "react-bootstrap/Card";
import React, {useState} from "react";

const  ShowNewestPost=(props)=>{

    const oldestTweet = props.tweets.find(tweet => tweet.authorId === props.oldest);


    return (

        <div>
            <Row className={"g-4"}>
                <Col sm={12} md={6} lg={4}>
                    <h3 style={{ color: "blue" }}>Oldest Post:</h3>
                    <CardGroup>
                        <Card style={{ width: '15rem', height: "350px" }}>
                            <Card.Img variant="top" src={"image.png"} style={{ width: "50px", height: "50px", display: "flex" }} />
                            <Card.Body>
                                <Card.Title>Tweet Author ID: {oldestTweet.authorId}</Card.Title>
                                <Card.Text>{oldestTweet.text}</Card.Text>
                                <p>Hashtag: #{oldestTweet.hashtag}</p>
                            </Card.Body>
                        </Card>
                    </CardGroup>
                </Col>
            </Row>
        </div>
    );

}
export default ShowNewestPost;