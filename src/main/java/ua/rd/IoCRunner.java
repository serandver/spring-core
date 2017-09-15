package ua.rd;

import ua.rd.domain.Tweet;
import ua.rd.ioc.ApplicationContext;
import ua.rd.ioc.Config;
import ua.rd.ioc.Context;
import ua.rd.ioc.JavaMapConfig;
import ua.rd.repository.InMemTweetRepository;
import ua.rd.repository.TweetRepository;
import ua.rd.services.SimpleTweetService;
import ua.rd.services.TweetService;

import java.util.HashMap;
import java.util.Map;

public class IoCRunner {
    public static void main(String[] args) {
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>(){{
                    put("tweet",
                            new HashMap<String, Object>(){{
                                put("type", Tweet.class);
                                put("isPrototype", true);
                            }});
                    put("tweetRepository",
                            new HashMap<String, Object>(){{
                                put("type", InMemTweetRepository.class);
                                put("isPrototype", true);
                            }});
                    put("tweetService",
                            new HashMap<String, Object>(){{
                                put("type", SimpleTweetService.class);
                                put("isPrototype", false);
                            }});
                }};
        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);
        TweetRepository tweetRepository = (TweetRepository)context.getBean("tweetRepository");
        TweetService tweetService = (TweetService) context.getBean("tweetService");

        System.out.println(tweetService.getClass());
        System.out.println(tweetRepository.allTweets());
        System.out.println(tweetService.allTweets());
    }
}
