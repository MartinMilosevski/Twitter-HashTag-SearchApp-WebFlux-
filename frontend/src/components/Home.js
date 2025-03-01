import React, {useState} from "react";
import axios from "axios";
import Card from 'react-bootstrap/Card';
import {CardGroup} from "react-bootstrap";
import {Col} from "react-bootstrap";
import {Row} from "react-bootstrap";


const Home =() => {

    const [selectedOption, SetSelectedOption] = useState(10)
    const [tweets,setTweets]=useState([])
    const [hashtag,setHashtag]=useState("")


    const takeTweetsFromBackend = async () =>{
        const response=axios.get(`http://localhost:8080/searchTweets/searchtweet?hashtag=${hashtag}`)
        setTweets((await response).data)
    }


    const handleOption = (event) => {
        SetSelectedOption(Number(event.target.value))
    };

    const handleHashtag =(event) =>{
        setHashtag(event.target.value)
    }

        return (


            <div>

                <input type={"search"} value={hashtag} placeholder={"Type hashtag..."}
                       onChange={handleHashtag}/>

                <select onChange={handleOption} value={selectedOption} >
                    <option value={10}>10</option>
                    <option value={15}>15</option>
                    <option value={20}>20</option>
                    <option value={25}>30</option>
                </select>

                <input type={"button"} value={"Search"} onClick={takeTweetsFromBackend}/>

                {tweets.length === 0 ? <p>No tweets Searched</p> :
                    <Row className={"g-4"}>
                        {tweets.map(tweet=>{
                            return(
                                <Col sm={12} md={6} lg={4} key={tweet.tweetId}>
                                    <CardGroup>
                                        <Card style={{ width: '15rem' }}>
                                            <Card.Img variant="top" src={"image.png"}
                                            style={{width:"50px",height:"50px",display:"flex"}}/>
                                            <Card.Body>
                                                <Card.Title>Tweet Author ID:
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
        )
    }
export default Home;