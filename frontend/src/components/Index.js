import React, {useEffect, useState} from "react";
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import axios from "axios";
import {CardGroup, Col, Row} from "react-bootstrap";
import Card from "react-bootstrap/Card";


const Index= ()=>{

    const [selectedOption, SetSelectedOption] = useState(10)
    const [tweets,setTweets]=useState([])
    const [hashtag,setHashtag]=useState("")
    const [showSearchDiv,setSearchDiv]=useState(true)
    const [metaDataForTweet,SetmetaDataForTweet]=useState({})

    useEffect(() => {
        if (metaDataForTweet) {
            console.log("Meta data updated:", metaDataForTweet);
        }
    }, [metaDataForTweet]);


    const takeTweetsFromBackend =  async () =>{
        const response=axios.get(`http://localhost:8080/searchTweets/searchtweet?hashtag=${hashtag}`)
        setTweets((await response).data)

        const response1=axios.get(`http://localhost:8080/searchTweets/metaDataForTweet?hashtag=${hashtag}`)
        SetmetaDataForTweet((await response1).data)
    }


    const handleOption = (event) => {
        SetSelectedOption(Number(event.target.value))
    };

    const handleHashtag =(event) =>{
        setHashtag(event.target.value)
    }

    const handleShowSearchDiv=()=>{
        setSearchDiv(!showSearchDiv)
    }

   /* const handleMetaDataForTweet =async ()=>{
        const response=axios.get(`http://localhost:8080/searchTweets/metaDataForTweet?hashtag=${hashtag}`)
        SetmetaDataForTweet((await response).data)
        console.log(metaDataForTweet)
    }*/


    return (


        <div id={"main_div"} className="min-h-screen bg-gradient-to-br from-blue-50 to-blue-100 flex justify-center items-center">
            <div className="container mx-auto px-4 py-8">
                <div className="flex items-center justify-center mb-12">

                    <h1 style={{fontFamily:"Courier New"}} className="text-4xl font-bold text-gray-800">Twitter Hashtag Search</h1>
                </div>

            <div>
                {showSearchDiv ?

                <div className="max-w-2xl mx-auto">

                        <div className="space-y-2">
                            <label htmlFor="hashtag" className="block text-sm font-medium text-gray-700">
                                Enter Hashtag
                            </label>
                            <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-gray-500">
                  #
                </span>
                                <input
                                    type="text"
                                    id="hashtag"
                                    className="col-form-label"
                                    placeholder="Enter hashtag without #"
                                    onChange={handleHashtag}
                                />
                            </div>
                        </div>

                        <div className="space-y-2">
                            <label htmlFor="postsCount" className="block text-sm font-medium text-gray-700">
                                Number of Posts
                            </label>
                            <select
                                id="postsCount"
                                className="col-form-label"
                                onChange={handleOption}
                            >
                                <option value="10">10 posts</option>
                                <option value="15">15 posts</option>
                                <option value="20">20 posts</option>
                                <option value="25">25 posts</option>
                                <option value="30">30 posts</option>
                            </select>
                        </div>

                        <button type="submit" className="btn btn-info"
                            onClick={()=>{
                                takeTweetsFromBackend();
                                handleShowSearchDiv()
                        }}>
                            Search Tweets
                        </button>



                    <div className="mt-8 text-center text-gray-600">
                        <p className="text-sm">
                            Enter a hashtag and select the number of posts to start searching
                        </p>
                    </div>

                </div> :
                    (
                        <button className={"btn btn-info"} onClick={()=>{handleShowSearchDiv();}}>
                            Search again
                        </button>
                    )
                }

                    <div>
                        {tweets.length === 0 ? <h1 style={{fontFamily:"PlayFair Display",padding:10,color:"blue"}}>No tweets Searched</h1> :
                            <Row className={"g-4"}>
                                {tweets.map(tweet=>{
                                    return(
                                        <Col sm={12} md={6} lg={4} key={tweet.tweetId}>
                                            <CardGroup>
                                                <Card style={{ width: '15rem',height:"350px" }}>
                                                    <Card.Img variant="top" src={"image.png"}
                                                              style={{width:"50px",height:"50px",display:"flex"}}/>
                                                    <Card.Body>
                                                        <Card.Title>
                                                            {metaDataForTweet.newestpostid === tweet.authorId ? <h1>Newest Post </h1> : null}
                                                            {console.log(metaDataForTweet.newestpostid)}
                                                            {console.log(tweet.authorId+"asdasd")}
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
                </div>
            </div>
        </div>


    );




}
export default Index;