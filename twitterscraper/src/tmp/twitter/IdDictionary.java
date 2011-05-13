package tmp.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 *
 * @author orensf
 */
public class IdDictionary {
    private Twitter twitter;

    public IdDictionary() {
        TwitterFactory twitterFactory = new TwitterFactory();
        this.twitter = twitterFactory.getInstance();
    }

    public String scrape(Long id) throws TwitterException {
        return twitter.showUser(id).getScreenName();
    }
}
