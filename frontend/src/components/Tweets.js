import {CardGroup, Col, Row} from "react-bootstrap";
import Card from "react-bootstrap/Card";
import React, {useState} from "react";
import {useQuery} from "react-query";
import axios from "axios";


const Tweets =(props)=> {



        return (

            <div>
                {props.tweets.length === 0 ?
                    <h1 style={{fontFamily: "PlayFair Display", padding: 10, color: "blue"}}>No tweets Searched</h1> :
                    <Row className={"g-4"}>
                        {props.tweets.map(tweet => {
                            return (

                                <Col sm={12} md={6} lg={4} key={tweet.tweetId}>

                                    <CardGroup>
                                        <Card style={{width: '15rem', height: "350px"}}>
                                            <Card.Img variant="top" src={"image.png"}
                                                      style={{width: "50px", height: "50px", display: "flex"}}/>
                                            <Card.Body>
                                                <Card.Title>
                                                    Tweet Author ID:
                                                    {tweet.authorId}</Card.Title>
                                                <Card.Text>
                                                    {tweet.text}
                                                </Card.Text>
                                                <p>Hashtag: #{tweet.hashtag}</p>
                                            </Card.Body>
                                        </Card>
                                    </CardGroup>
                                </Col>
                            );
                        })}
                    </Row>
                }
            </div>
        );
}

export default Tweets;