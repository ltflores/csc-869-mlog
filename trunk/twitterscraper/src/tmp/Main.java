package tmp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import tmp.twitter.FriendScraper;
import tmp.twitter.IScraper;
import twitter4j.TwitterException;
import tmp.splitters.WordSplitter;
import tmp.twitter.TweetIntervalScraper;

/**
 *
 * @author orensf
 */
public class Main {
    public static void main(String[] args) {
        Logger.init(Logger.Level.WARNING, System.err);
        Arguments arguments = new Arguments(args);
        Logger.init(arguments.getLogLevel(), System.err);
        ToolFactory toolFactory = new ToolFactory(arguments);
        switch (arguments.getTarget()) {
            case ScrapeTweetInterval:
                TweetIntervalScraper intervalScraper = null;
                if (arguments.getPositiveAttitude()!=null) {
                    intervalScraper
                            = new TweetIntervalScraper(arguments.getSinceDate(),
                                                       arguments.getUntilDate(),
                                                       arguments.getPositiveAttitude().booleanValue());
                } else {
                    intervalScraper
                        = new TweetIntervalScraper(arguments.getSinceDate(),
                                                   arguments.getUntilDate());
                }
                intervalScraper.setPerUserLimit(arguments.getPerUserLimit());
                intervalScraper.setWithRetweets(arguments.getWithRetweets());
                for (String screenName : arguments.getScreenNames()) {
                    String[] tweets = null;
                    while (true) {
                        try {
                            tweets = intervalScraper.scrape(screenName);
                            // if no exception occured:
                            break;
                        } catch (TwitterException te) {
                            if (te.exceededRateLimitation()) {
                                int secondsToSleep = te.getRetryAfter();
                                Logger.log(Logger.Level.WARNING, "recieved "
                                        + "TwitterException while scrapeing "
                                        + "tweets of @" + screenName
                                        + ". Backing off for " + secondsToSleep
                                        + " seconds.");
                                try {
                                    Thread.sleep(secondsToSleep * 1000);
                                } catch (InterruptedException ex) {
                                    Logger.log(Logger.Level.ERROR, "oh no, "
                                            + "caught exception while "
                                            + "sleeping:\n");
                                    ex.printStackTrace();
                                    System.exit(-1);
                                }
                                continue; // try to scrape again
                            } else { // twitter exception not related to rate
                                Logger.log(Logger.Level.ERROR, "oh no, "
                                        + "caught twitter exception not "
                                        + "related to quotas, while scraping "
                                        + "tweets of " + screenName + ":\n");
                                te.printStackTrace();
                                Logger.log(Logger.Level.WARNING, "not scraping "+screenName);
                                break;
                            }
                        }
                    }
                    if (tweets!=null && tweets.length>0) {
                        for (String tweet : tweets) {
                            if (tweet == null) {
                                Logger.log(Logger.Level.ERROR, "null tweet for: " + screenName);
                            } else if (tweet.isEmpty()) {
                                Logger.log(Logger.Level.ERROR, "empty tweet for: " + screenName);
                            } else {
                                System.out.println(tweet);
                            }
                        }
                    }
                }
                break;
            case ScrapeFriends:
                IScraper<Long[]> friendScraper = new FriendScraper(); //toolFactory.getScraper();
                for (String screenName : arguments.getScreenNames()) {
                    Long[][] edges = null;
                    while (true) {
                        try {
                            edges = friendScraper.scrape(screenName);
                            // if no exception occured:
                            break;
                        } catch (TwitterException te) {
                            if (te.exceededRateLimitation()) {
                                int secondsToSleep = te.getRetryAfter();
                                Logger.log(Logger.Level.WARNING, "recieved "
                                        + "TwitterException while scrapeing "
                                        + "friends of @" + screenName
                                        + ". Backing off for " + secondsToSleep
                                        + " seconds.");
                                try {
                                    Thread.sleep(secondsToSleep * 1000);
                                } catch (InterruptedException ex) {
                                    Logger.log(Logger.Level.ERROR, "oh no, "
                                            + "caught exception while "
                                            + "sleeping:\n");
                                    ex.printStackTrace();
                                    System.exit(-1);
                                }
                                continue; // try to scrape again
                            } else { // twitter exception not related to rate
                                Logger.log(Logger.Level.ERROR, "oh no, "
                                        + "caught twitter exception not "
                                        + "related to quotas, while scraping "
                                        + "friends of " + screenName + ":\n");
                                te.printStackTrace();
                                Logger.log(Logger.Level.WARNING, "not scraping "+screenName);
                                break;
                            }
                        }
                    }
                    if (edges!=null && edges.length>0) {
                        for (Long[] edge : edges) {
                            if (edge == null) {
                                Logger.log(Logger.Level.ERROR, "null edge for: " + screenName);
                            } else if (edge.length == 0) {
                                Logger.log(Logger.Level.ERROR, "empty edge for: " + screenName);
                            } else if (edge.length == 1) {
                                Logger.log(Logger.Level.ERROR, "edge with only one vertex for: " + screenName);
                            } else {
                                System.out.println(edge[0] + " " + edge[1]);
                            }
                        }
                    }
                }
                break;
            case Tokenize:
                WordSplitter splitter = new WordSplitter();

                for (String dataFileName : arguments.getDataFileNames()) {
                    try {
                        File dataFile = new File(dataFileName);
                        PrintWriter out = new PrintWriter("clean/" + dataFile.getName());
                        Scanner scanner = new Scanner(dataFile);
                        while (scanner.hasNextLine()) {
                            String rawLine = scanner.nextLine();
                            String[] tokens = splitter.split(rawLine);
                            StringBuilder cleanLine = new StringBuilder();
                            for (String token : tokens) {
                                cleanLine.append(token);
                                cleanLine.append(" ");
                            }
                            out.println(cleanLine.toString());

                        }
                        out.close();
                    } catch(FileNotFoundException e) {
                        Logger.log(Logger.Level.ERROR, "could not load "
                                + "data from file: " + dataFileName);
                    }
                }
                break;
            default: Logger.log(Logger.Level.ERROR, "this target has "
                    + "not been implemented yet.");

        }
        

    }


}
