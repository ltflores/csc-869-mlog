package tmp.twitter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private int perUserLimit = -1; // no limit
    private boolean withRetweets = true;
    private boolean prependScreenName = false;
    private boolean prependTimestamp = false;
    private DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public TweetIntervalScraper(Date sinceDate, Date untilDate) {
        TwitterFactory twitterFactory = new TwitterFactory();
        this.twitter = twitterFactory.getInstance();
        this.queryStrBase = "since:" + this.dateFormatter.format(sinceDate)
                + " until:" + this.dateFormatter.format(untilDate);
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

    public void setPrependTimestamp(boolean prependTimestamp) {
        this.prependTimestamp = prependTimestamp;
    }

    public void setPrependScreenName(boolean prependScreenName) {
        this.prependScreenName = prependScreenName;
    }

    public String[] scrape(String screenName) throws TwitterException {
        ArrayList<String> result = new ArrayList<String>();
        String queryStr = this.queryStrBase + " from:"+screenName;
        List<Tweet> tweets = twitter.search(new Query(queryStr)).getTweets();
        for (Tweet tweet : tweets) {
            String tweetText = tweet.getText();
            if (this.withRetweets || !tweetText.startsWith("RT ")) {
                if (this.prependTimestamp) {
                    tweetText = this.dateFormatter.format(tweet.getCreatedAt())
                            + " " + tweetText;
                }
                if (this.prependScreenName) {
                    tweetText = "@" + screenName + " " + tweetText;
                }
                result.add(tweetText);
                if (this.perUserLimit >= 0 && result.size() >= this.perUserLimit) {
                    break;
                }
            }
        }
        return result.toArray(new String[0]);
    }

}
