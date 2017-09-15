package ua.rd;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ua.rd.domain.Tweet;
import ua.rd.repository.TweetRepository;
import ua.rd.services.TweetService;

import java.util.Arrays;

public class SpringXMLConfigRunner {
    public static void main(String[] args) {
        ConfigurableApplicationContext repoContext
                = new ClassPathXmlApplicationContext("repositoryContext.xml");

        ConfigurableApplicationContext serviceContext
                = new ClassPathXmlApplicationContext(
                        new String[]{"serviceContext.xml"}, repoContext);

        System.out.println(Arrays.toString(repoContext.getBeanDefinitionNames()));
        System.out.println(Arrays.toString(serviceContext.getBeanDefinitionNames()));

        TweetService tweetService = (TweetService) serviceContext.getBean("tweetService");
        System.out.println(tweetService.allTweets());

        TweetRepository repository = (TweetRepository) repoContext.getBean("tweetRepository");
        System.out.println(repository.allTweets());

        Tweet tweet = (Tweet) repoContext.getBean("tweet");
        System.out.println(tweet);

        BeanDefinition bd = repoContext.getBeanFactory().getBeanDefinition("tweetRepository");
        System.out.println(bd.toString());

        serviceContext.close();
        repoContext.close();
    }
}
