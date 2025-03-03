import {CardGroup, Col, Row} from "react-bootstrap";
import Card from "react-bootstrap/Card";
import React, {useState} from "react";

const  ShowNewestPost=(props)=>{

    const newestTweet = props.tweets.find(tweet => tweet.authorId === props.newest);


    return (

        <div>
            <Row className={"g-4"}>
                <Col sm={12} md={6} lg={4}>
                    <h3 style={{ color: "blue" }}>Newest Post:</h3>
                    <CardGroup>
                        <Card style={{ width: '15rem', height: "350px" }}>
                            <Card.Img variant="top" src={"image.png"} style={{ width: "50px", height: "50px", display: "flex" }} />
                            <Card.Body>
                                <Card.Title>Tweet Author ID: {newestTweet.authorId}</Card.Title>
                                <Card.Text>{newestTweet.text}</Card.Text>
                                <p>Hashtag: #{newestTweet.hashtag}</p>
                            </Card.Body>
                        </Card>
                    </CardGroup>
                </Col>
            </Row>
        </div>
    );

}
export default ShowNewestPost;