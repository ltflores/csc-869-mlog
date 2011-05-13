package tmp.twitter;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 *
 * @author orensf
 */
public class FriendScraper implements IScraper<Long[]> {
    private Twitter twitter;

    public FriendScraper() {
        TwitterFactory twitterFactory = new TwitterFactory();
        this.twitter = twitterFactory.getInstance();
    }

    public Long[][] scrape(String screenName) throws TwitterException {
        final long selfId = twitter.showUser(screenName).getId();
        ArrayList<Long[]> result = new ArrayList<Long[]>();
        IDs ids = null;
        for (long cursor = -1; cursor!=0; cursor=ids.getNextCursor()) {
            ids = twitter.getFriendsIDs(screenName, cursor);
            final long[] friendIds = ids.getIDs();
            /*
            result.addAll(new AbstractList<Long[]>() {
                public Long[] get(int i) {return new Long[]{selfId,friendIds[i]};}
                public int size() {return friendIds.length;}
                public Long[] set(int i, Long[] l) {
                    Long oldId = friendIds[i];
                    friendIds[i]=l[1];
                    return new Long[]{selfId,oldId};
                }
            });
             */
            for (long friendId : friendIds) {
                result.add(new Long[]{selfId,friendId});
            }
        }
        return result.toArray(new Long[2][0]);
    }

}
