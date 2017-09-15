package ua.rd.services;

import ua.rd.domain.Tweet;
import ua.rd.repository.TweetRepository;

public class SimpleTweetService implements TweetService {

    private TweetRepository tweetRepository;
    private Tweet tweet = new LazyProxy("tweet").getInstance();

    public void setTweet(Tweet tweet) {
        this.tweet = tweet;
    }

    public SimpleTweetService() {
    }
        //если tweet указан как prototype вернуть новый экземпляр твита

    /*
    LazyProxy extend Tweet{
        String bean;
        getInstance() {
            appContext.getBean(bean);
        }
    }

    */

    public SimpleTweetService(TweetRepository tweetRepository, Tweet tweet) {
        this.tweetRepository = tweetRepository;
        this.tweet = tweet;
    }

    @Override
    public Iterable<Tweet> allTweets() {
        return tweetRepository.allTweets();
    }

    @Override
    public Tweet newTweet() {
        return tweet;
    }
}
