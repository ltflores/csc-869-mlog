package tmp;
import tmp.Logger.Level; // do not import java's Level
import tmp.Logger; // do not import java's Logger

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
            case BuildDirectory:
                if (!arguments.outputToDir()) {
                    Logger.log(Logger.Level.ERROR,
                            "must provide --output-dir for --target=build-directory");
                    System.exit(-1);
                }
                if (arguments.getDataFileNames().isEmpty()) {
                    Logger.log(Logger.Level.ERROR, "no --data provided for --target=build-directory");
                    System.exit(-1);
                }
                for (String dataFileName : arguments.getDataFileNames()) {
                    Scanner in = null;
                    FileWriter out = null;
                    try {
                        File dataFile = new File(dataFileName);
                        in = new Scanner(dataFile);
                     } catch(FileNotFoundException e) {
                        Logger.log(Logger.Level.ERROR, "could not load "
                                + "data from file: " + dataFileName);
                    }
                    int lineCounter = 0;
                    while (in.hasNextLine()) {
                        lineCounter++;
                        String rawLine = in.nextLine();
                        Scanner lineScanner = new Scanner(rawLine);
                        String outFileName = "";
                        if (!lineScanner.hasNext()) {
                            Logger.log(Logger.Level.WARNING,
                                    "empty line " + lineCounter
                                    + " is not prefixed with a screen "
                                    + "name, in data file: "
                                    + dataFileName);
                            Logger.log(Logger.Level.WARNING,
                                    "ignoring line " + lineCounter);
                            continue;
                        } else {
                            outFileName = lineScanner.next();
                            if (!outFileName.startsWith("@")) {
                                Logger.log(Logger.Level.WARNING,
                                        "line " + lineCounter
                                        + " is not prefixed with a screen "
                                        + "name, in data file: "
                                        + dataFileName
                                        + ". it will be appended to last output");
                                try {
                                    if (out!=null) {
                                        out.append(" " + rawLine);
                                        continue;
                                    } else {
                                        throw new IOException(); // cant append
                                    }
                                } catch (IOException ex) {
                                    Logger.log(Logger.Level.WARNING,
                                            "could not append line "
                                            + lineCounter
                                            + " to output");
                                    Logger.log(Logger.Level.WARNING,
                                            "ignoring line " + lineCounter);
                                    continue;
                                }
                            } else {
                                if (arguments.getPrependTimestamp()) {
                                    if (!lineScanner.hasNext()) {
                                        Logger.log(Logger.Level.WARNING,
                                                "line " + lineCounter
                                                + " is not prefixed with a "
                                                + "timestamp, so it will be "
                                                + "appended to previous line.");
                                        try {
                                            if (out != null) {
                                                out.append(" " + rawLine);
                                                continue;
                                            } else {
                                                throw new IOException(); // cant append
                                            }
                                        } catch (IOException ex) {
                                            Logger.log(Logger.Level.WARNING,
                                                    "could not append line "
                                                    + lineCounter
                                                    + " to output");
                                            Logger.log(Logger.Level.WARNING,
                                                    "ignoring line " + lineCounter);
                                            continue;
                                        }
                                    } else {
                                        String timeStamp = lineScanner.next();
                                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                        try {
                                            Date timeStampDate = df.parse(timeStamp);
                                            if (timeStampDate == null) {
                                                throw new ParseException("", 0);
                                            }
                                        } catch (ParseException ex) {
                                            Logger.log(Logger.Level.WARNING,
                                                    "line " + lineCounter
                                                    + " is not prefixed with a "
                                                    + "timestamp, so it will "
                                                    + "be appended to "
                                                    + "previous line");
                                            try {
                                                if (out != null) {
                                                    out.append(" " + rawLine);
                                                    continue;
                                                } else {
                                                    // can't append
                                                    throw new IOException();
                                                }
                                            } catch (IOException e) {
                                                Logger.log(Logger.Level.WARNING,
                                                        "could not append line "
                                                        + lineCounter
                                                        + " to output");
                                                Logger.log(Logger.Level.WARNING,
                                                        "ignoring line " + lineCounter);
                                                continue;
                                            }
                                        }
                                    }
                                }
                                // at this point we have found both @screenName
                                //   and a timestamp, so we can close
                                //   previous file and open a new out
                                if (out!=null) {
                                    try {
                                        out.append("\n");
                                        out.close();
                                    } catch (IOException ex) {
                                        Logger.log(Logger.Level.WARNING,
                                                "could not close output. line "
                                                + (lineCounter - 1)
                                                + " may be lost.");
                                    }
                                }
                                outFileName 
                                        = arguments.getOutputDir().getAbsolutePath()
                                        + File.separator
                                        + outFileName.substring(1);
                                File outFile = new File(outFileName);
                                try {
                                    out = new FileWriter(outFile, true);
                                } catch (FileNotFoundException ex) {
                                    Logger.log(Logger.Level.ERROR,
                                            "could not create file: " + outFileName);
                                    Logger.log(Logger.Level.WARNING,
                                            "ignoring line " + lineCounter);
                                    continue;
                                } catch (IOException e) {
                                    Logger.log(Logger.Level.ERROR,
                                            "could not create file: " + outFileName);
                                    Logger.log(Logger.Level.WARNING,
                                            "ignoring line " + lineCounter);
                                    continue;
                                }
                                try {
                                    out.append(lineScanner.nextLine().trim());
                                } catch (IOException ex) {
                                    Logger.log(Logger.Level.WARNING,
                                            "cannot append tweet to file: "
                                            + outFileName);
                                    Logger.log(Logger.Level.WARNING,
                                            "ignoring line " + lineCounter);
                                    continue;
                                }
                            }

                        }
                    } // line loop
                    try {
                        out.close();
                    } catch (IOException ex) {
                        Logger.log(Logger.Level.WARNING,
                                "could not close output file. "
                                + "last line may be lost");
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
            default:
                Logger.log(Logger.Level.ERROR, "this target has "
                    + "not been implemented yet.");
                System.exit(-1);

        }
        

    }


}
