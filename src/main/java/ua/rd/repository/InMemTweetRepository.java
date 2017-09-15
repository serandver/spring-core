package ua.rd.repository;

import ua.rd.domain.Tweet;

import java.util.Arrays;
import java.util.List;

public class InMemTweetRepository implements TweetRepository {

    private List<Tweet> tweets;

    public void init() {
        tweets = Arrays.asList(
                new Tweet(1L, "1st Message", null),
                new Tweet(2L, "2nd Message", null)
        );
    }

    @Override
    public Iterable<Tweet> allTweets() {
        return tweets;
    }


}
