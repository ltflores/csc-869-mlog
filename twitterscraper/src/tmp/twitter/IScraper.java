package tmp.twitter;

import twitter4j.TwitterException;

/**
 *
 * @author orensf
 */
public interface IScraper<T> {
    public T[] scrape(String screenName) throws TwitterException;

}
