package tmp.twitter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 *
 * @author orensf
 */
public class TimelineScraper {
    private Twitter twitter;
    private int perUserLimit = -1; // no limit
    private boolean withRetweets = true;
    private boolean prependTimestamp = false;
    private boolean prependScreenName = false;
    private DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private Date sinceDate;
    private Date untilDate;
    private long sinceId = -1L;
    private long maxId = -1L;


    public TimelineScraper() {
        TwitterFactory twitterFactory = new TwitterFactory();
        this.twitter = twitterFactory.getInstance();
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

    public void setSinceId(long sinceId) {
        this.sinceId = sinceId;
    }

    public void setMaxId(long maxId) {
        this.maxId = maxId;
    }

    public String[] scrape(String screenName) throws TwitterException {
        ArrayList<String> result = new ArrayList<String>();
        int tweetTotal = twitter.showUser(screenName).getStatusesCount();
        int tweetsPerPage = 20;
        int maxPage1 = tweetTotal/tweetsPerPage;
        if ((tweetTotal % tweetsPerPage)>0) {
            // add partial last page
            maxPage1++;
        }
        int maxPage2 = this.perUserLimit / tweetsPerPage;
        if ((this.perUserLimit % tweetsPerPage)>0) {
            // add partial last page
            maxPage2++;
        }
        int maxPage = maxPage1>maxPage2 ? maxPage2 : maxPage1; // =min(max1,max2)

        pageLoop:
        for (int pageNumber=1; pageNumber<=maxPage; pageNumber++) {
            Paging page = new Paging(pageNumber, tweetsPerPage);
            if (this.sinceId>=0) {
                page.setSinceId(this.sinceId);
            }
            if (this.maxId>=0) {
                page.setMaxId(this.maxId);
            }
            List<Status> tweets = this.twitter.getUserTimeline(screenName,page);
            for (Status tweet : tweets) {
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
                        break pageLoop;
                    }
                }
            }
        }
        return result.toArray(new String[0]);
    }

}
