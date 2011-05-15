package tmp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;
import tmp.twitter.FriendScraper;
import tmp.twitter.IScraper;
import twitter4j.TwitterException;
import tmp.splitters.WordSplitter;
import tmp.twitter.TimelineScraper;
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
        int progressCounter = 0;
        int progressTotal;
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
                intervalScraper.setPrependScreenName(arguments.getPrependScreenName());
                intervalScraper.setPrependTimestamp(arguments.getPrependTimestamp());

                progressCounter = 0;
                progressTotal = arguments.getScreenNames().size();

                for (String screenName : arguments.getScreenNames()) {
                    Logger.log(Logger.Level.WARNING,
                            "" + (100*progressCounter/progressTotal)
                            + "% done, now scraping: "+screenName);
                    progressCounter++;

                    String[] tweets = null;

                    BufferedWriter output=null;

                    if (arguments.outputToDir()){
                    	try {
                    		String fileName = screenName;
                	    	output = new BufferedWriter(new FileWriter(arguments.getOutputDir().getAbsolutePath() + File.separator + fileName));
                    	} catch (Exception e){
                        	e.printStackTrace();
                        }
                    }

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

                    //time to filter/print/write tweets
                    if (tweets!=null && tweets.length>0) {
                        for (String tweet : tweets) {
                            if (tweet == null) {
                                Logger.log(Logger.Level.ERROR, "null tweet for: " + screenName);
                            } else if (tweet.isEmpty()) {
                                Logger.log(Logger.Level.ERROR, "empty tweet for: " + screenName);
                            } else {
                            	if (arguments.outputToDir()){
                            		try {
                            			output.write(tweet+"\n");
                            		} catch(Exception e) {
                            			e.printStackTrace();
                            		}
                            	}
                                System.out.println(tweet);
                            }
                        }
                    }
                    if (arguments.outputToDir()){
                    	try {
                    		output.flush();
                	    	output.close();
                    	} catch(Exception e){
                    		e.printStackTrace();
                    	}
                    }

                }
                break;
            case ScrapeTimeline:
                TimelineScraper timelineScraper = new TimelineScraper();
                timelineScraper.setPerUserLimit(arguments.getPerUserLimit());
                timelineScraper.setWithRetweets(arguments.getWithRetweets());
                timelineScraper.setPrependScreenName(arguments.getPrependScreenName());
                timelineScraper.setPrependTimestamp(arguments.getPrependTimestamp());
                timelineScraper.setMaxId(arguments.getMaxId());
                timelineScraper.setSinceId(arguments.getSinceId());

                progressCounter = 0;
                progressTotal = arguments.getScreenNames().size();
                
                for (String screenName : arguments.getScreenNames()) {
                    Logger.log(Logger.Level.WARNING,
                            "" + (100*progressCounter/progressTotal)
                            + "% done, now scraping: "+screenName);
                    progressCounter++;

                    String[] tweets = null;
                    
                    BufferedWriter output=null;
                    
                    if (arguments.outputToDir()){
                    	try {
                    		String fileName = screenName;
                	    	output = new BufferedWriter(new FileWriter(arguments.getOutputDir().getAbsolutePath() + File.separator + fileName));
                    	} catch (Exception e){
                        	e.printStackTrace();
                        }
                    }
                    
                    while (true) {
                        try {
                            tweets = timelineScraper.scrape(screenName);
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
                    
                    //time to filter/print/write tweets
                    if (tweets!=null && tweets.length>0) {
                        for (String tweet : tweets) {
                            if (tweet == null) {
                                Logger.log(Logger.Level.ERROR, "null tweet for: " + screenName);
                            } else if (tweet.isEmpty()) {
                                Logger.log(Logger.Level.ERROR, "empty tweet for: " + screenName);
                            } else {
                            	if (arguments.outputToDir()){
                            		try {
                            			output.write(tweet+"\n");
                            		} catch(Exception e) {
                            			e.printStackTrace();
                            		}
                            	}
                                System.out.println(tweet);
                            }
                        }
                    }
                    if (arguments.outputToDir()){
                    	try {
                    		output.flush();
                	    	output.close();
                    	} catch(Exception e){
                    		e.printStackTrace();
                    	}
                    }
                    
                }
                break;
            case ScrapeFriends:
                IScraper<Long[]> friendScraper = new FriendScraper(); //toolFactory.getScraper();
                progressCounter = 0;
                progressTotal = arguments.getScreenNames().size();
                for (String screenName : arguments.getScreenNames()) {
                    Logger.log(Logger.Level.WARNING,
                            "" + (100*progressCounter/progressTotal)
                            + "% done, now scraping: "+screenName);
                    progressCounter++;
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
