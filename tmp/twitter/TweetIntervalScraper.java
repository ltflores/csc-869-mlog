package twitter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import main.Logger.Level;
import main.Logger;
import twitter4j.Query;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 *
 * @author orensf
 */
public class TweetIntervalScraper {
    private Twitter twitter;
    private String queryStrBase;
    private int perUserLimit;
    private boolean withRetweets;

    public TweetIntervalScraper(Date sinceDate, Date untilDate) {
        TwitterFactory twitterFactory = new TwitterFactory();
        this.twitter = twitterFactory.getInstance();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        this.queryStrBase = "since:" + dateFormatter.format(sinceDate)
                + " until:" + dateFormatter.format(untilDate);
        this.withRetweets = true;
        this.perUserLimit = -1; // no limit
    }

    public TweetIntervalScraper(Date sinceDate,
            Date untilDate, boolean isPositiveAttitude) {
        this(sinceDate,untilDate);
        if (isPositiveAttitude) {
            this.queryStrBase += " :)";
        } else {
            this.queryStrBase += " :(";
        }
    }

    public void setPerUserLimit(int limit) {
        this.perUserLimit = limit;
    }

    public void setWithRetweets(boolean withRetweets) {
        this.withRetweets = withRetweets;
    }

    public String[] scrape(String screenName) throws TwitterException {
        ArrayList<String> result = new ArrayList<String>();
        String queryStr = this.queryStrBase + " from:"+screenName;
        List<Tweet> tweets = twitter.search(new Query(queryStr)).getTweets();
        for (Tweet tweet : tweets) {
            String tweetText = tweet.getText();
            if (this.withRetweets || !tweetText.startsWith("RT ")) {
                result.add(tweet.getText());
                if (this.perUserLimit >= 0 && result.size() >= this.perUserLimit) {
                    break;
                }
            }
        }
        return result.toArray(new String[0]);
    }

}
