package ua.rd.repository;

import ua.rd.domain.Tweet;
import ua.rd.ioc.Benchmark;

public interface TweetRepository {

    @Benchmark
    Iterable<Tweet> allTweets();

}
