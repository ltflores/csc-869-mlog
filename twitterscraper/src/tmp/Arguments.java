package tmp;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * parses command line arguments and stores them
 * @author orensf
 */
public class Arguments {

    // prefixes:
    private static final String ARG_DATA = "--data=";
    private static final String ARG_TARGET = "--target=";
    private static final String ARG_DEBUG_ALL = "--debug=all";
    private static final String ARG_SCREEN_NAME_PATH = "--screen-name-path=";
    private static final String ARG_SCREEN_NAME = "--screen-name=";
    private static final String ARG_SINCE_DATE = "--since-date=";
    private static final String ARG_UNTIL_DATE = "--until-date=";
    private static final String ARG_PER_USER_LIMIT = "--per-user-limit=";
    private static final String ARG_POSITIVE = "--positive";
    private static final String ARG_NEGATIVE = "--negative";
    private static final String ARG_NO_RETWEETS = "--no-retweets";
    private static final String ARG_OUTPUT_DIR = "--output-dir=";
    private static final String ARG_PREPEND_SCREEN_NAME = "--prepend-screen-name";
    private static final String ARG_PREPEND_TIMESTAMP = "--prepend-timestamp";
    private static final String ARG_SINCE_ID = "--since-id=";
    private static final String ARG_MAX_ID = "--max-id=";


    // defaults:
    private Set<String> dataFileNames = new HashSet<String>();
    private Set<String> screenNames = new HashSet<String>();
    private Target target = Target.All;
    private Logger.Level logLevel = Logger.Level.ERROR;
    private DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private Date sinceDate = new Date(0L); // the big bang
    private Date untilDate = new Date(); // now
    private int perUserLimit = -1; // no limit
    private Boolean positiveAttitude = null; // no polarity
    private boolean withRetweets = true;
    private boolean outputToDir = false;
    private File outputDir = null;
    private boolean prependScreenName = false;
    private boolean prependTimestamp = false;
    private long sinceId = -1L; // no limit
    private long maxId = -1L; // no limit

    
    public Arguments(String[] args) {
        this.parseArguments(args);
        // TODO: perform sanity check here?
    }

    public final void parseArguments(String[] args) {
        for (String arg : args) {
            if (arg.startsWith(ARG_TARGET)) {
                String targetStr = arg.substring(ARG_TARGET.length());
                this.target = Target.getTarget(targetStr);
                if (this.target == null) {
                    Logger.log(Logger.Level.WARNING, "could not find requested target: "
                            + targetStr + ". forcing --target="
                            + Target.All.getName() + ".");
                    this.target = Target.All;
                }
            } else if (arg.startsWith(ARG_DATA)) {
                Collection<String> morePaths
                        = this.parseStringList(arg.substring(ARG_DATA.length()),
                                               File.pathSeparator);
                this.dataFileNames.addAll(this.parsePathList(morePaths));
            } else if (arg.startsWith(ARG_SCREEN_NAME_PATH)) {
                Collection<String> paths
                        = this.parseStringList(arg.substring(ARG_SCREEN_NAME_PATH.length()),
                                               File.pathSeparator);
                Set<String> fileNames = new HashSet<String>();
                fileNames.addAll(this.parsePathList(paths));
                this.screenNames.addAll(this.parseScreenNameFiles(fileNames));
            } else if (arg.startsWith(ARG_SCREEN_NAME)) {
                String screenNameList = arg.substring(ARG_SCREEN_NAME.length());
                this.screenNames.addAll(this.parseScreenNameList(screenNameList));
            } else if (arg.startsWith(ARG_DEBUG_ALL)) {
                this.logLevel = Logger.Level.WARNING;
            } else if (arg.startsWith(ARG_SINCE_DATE)) {
                String dateStr = arg.substring(ARG_SINCE_DATE.length());
                try {
                    this.sinceDate = this.dateFormatter.parse(dateStr);
                } catch (ParseException ex) {
                    Logger.log(Logger.Level.ERROR,
                            "could not parse since-date: " + dateStr);
                    this.sinceDate = new Date(0L); // resotring default value
                }
            } else if (arg.startsWith(ARG_UNTIL_DATE)) {
                String dateStr = arg.substring(ARG_UNTIL_DATE.length());
                try {
                    this.untilDate = this.dateFormatter.parse(dateStr);
                } catch (ParseException ex) {
                    Logger.log(Logger.Level.ERROR,
                            "could not parse until-date: " + dateStr);
                    this.untilDate = new Date(); // resotring default value
                }
            } else if (arg.startsWith(ARG_PER_USER_LIMIT)) {
                this.perUserLimit
                        = Integer.parseInt(arg.substring(ARG_PER_USER_LIMIT.length()));
            } else if (arg.startsWith(ARG_POSITIVE)) {
                this.positiveAttitude = true;
            } else if (arg.startsWith(ARG_NEGATIVE)) {
                this.positiveAttitude = false;
            } else if (arg.startsWith(ARG_NO_RETWEETS)) {
                this.withRetweets = false;
            } else if (arg.startsWith(ARG_OUTPUT_DIR)) {
            	this.outputToDir = true;
            	String outputDirName = arg.substring(ARG_OUTPUT_DIR.length());
            	this.outputDir = new File(outputDirName);
            	outputDir.mkdir();
            } else if (arg.startsWith(ARG_PREPEND_SCREEN_NAME)) {
                this.prependScreenName = true;
            } else if (arg.startsWith(ARG_PREPEND_TIMESTAMP)) {
                this.prependTimestamp = true;
            } else if (arg.startsWith(ARG_SINCE_ID)) {
                this.sinceId = Long.parseLong(arg.substring(ARG_SINCE_ID.length()));
            } else if (arg.startsWith(ARG_MAX_ID)) {
                this.maxId = Long.parseLong(arg.substring(ARG_MAX_ID.length()));
            } else {
                Logger.log(Logger.Level.ERROR, "unknown argument: "+arg);
                System.exit(1);
            }
        }
    }

    /**
     * helper method to parse an argument string
     * into a list using the given delimeter
     * @param strToParse
     * @param delimeter
     * @return
     */
    private Collection<String> parseStringList(String strToParse, String delimeter) {
        Collection<String> result = new ArrayList<String>();
        Scanner parser = new Scanner(strToParse);
        parser.useDelimiter(delimeter);
        while (parser.hasNext()) {
            String candidateStr = parser.next().trim();
            if (!candidateStr.isEmpty()) {
                result.add(candidateStr);
            }
        }
        return result;
    }

    /**
     * helper method to convert a list of paths into file names.
     * in particular, we add all files in each directory in the given list
     * @param pathList
     * @return
     */
    private Collection<String> parsePathList(Collection<String> pathList) {
        Collection<String> result = new ArrayList<String>();
        for (String path : pathList) {
            File file = new File(path);
            if (!file.exists()) {
                Logger.log(Logger.Level.WARNING,
                        "cannot find data in path: " + path);
                continue;
            }
            // else, if it exists:
            if (file.isDirectory()) {
                for (String fileName : file.list()) {
                    result.add(path + fileName);
                    //Logger.log(Logger.Level.WARNING, "adding: " + fileName);
                }
            } else if (file.isFile()) {
                result.add(path);
            }
        }
        return result;
    }

    /**
     * extracting twitter screen names of the form @someone
     * from each of the given files
     * @param fileNames
     * @return
     */
    private Collection<String> parseScreenNameFiles(Collection<String> fileNames) {
        Pattern screenNamePattern = Pattern.compile("@\\w+");
        Set<String> result = new HashSet<String>();
        for (String fileName : fileNames) {
            Scanner scanner = null;
            try {
                scanner = new Scanner(new File(fileName));

            } catch (FileNotFoundException ex) {
                Logger.log(Logger.Level.ERROR, "cannot read file: " + fileName);
            }

            while (scanner.hasNext()) {
                String strToSearch = scanner.next();
                Matcher matcher = screenNamePattern.matcher(strToSearch);
                while (matcher.find()) {
                    result.add(strToSearch.substring(matcher.start()+1, matcher.end()));
                }
            }
        }
        return result;
    }

    private Collection<String> parseScreenNameList(String screenNameList) {
        Pattern screenNamePattern = Pattern.compile("@\\w+");
        Set<String> result = new HashSet<String>();
        Matcher matcher = screenNamePattern.matcher(screenNameList);
        while (matcher.find()) {
            result.add(screenNameList.substring(matcher.start() + 1, matcher.end()));
        }
        return result;
    }

    // getters methods:

    /**
     *
     * @return
     */
    public Collection<String> getDataFileNames() {
        return this.dataFileNames;
    }

    /**
     *
     * @return
     */
    public Target getTarget() {
        return this.target;
    }

    /**
     *
     * @return
     */
    public Logger.Level getLogLevel() {
        return this.logLevel;
    }

    /**
     * retrieve twitter screen names collected from all paths, files and command line strings
     * @return
     */
    public Collection<String> getScreenNames() {
        return this.screenNames;
    }

    /**
     *
     * @return
     */
    public Date getSinceDate() {
        return this.sinceDate;
    }

    /**
     *
     * @return
     */
    public Date getUntilDate() {
        return this.untilDate;
    }

    /**
     * returns true/false if scraping positive/negative attitude tweets,
     * or null for no polarity.
     * @return
     */
    public Boolean getPositiveAttitude() {
        return this.positiveAttitude;
    }

    /**
     * returns limit for scraped tweets/friends per user,
     * or a negative integer if unlimited.
     * @return
     */
    public int getPerUserLimit() {
        return this.perUserLimit;
    }

    public boolean getWithRetweets() {
        return this.withRetweets;
    }
    
    public boolean outputToDir(){
    	return this.outputToDir;
    }
    
    public File getOutputDir(){
    	return this.outputDir;
    }

    public boolean getPrependScreenName() {
        return this.prependScreenName;
    }

    public boolean getPrependTimestamp() {
        return this.prependTimestamp;
    }

    /**
     * returns id of first tweet to scrape,
     * or a negative number if no lower limit should be set
     * @return
     */
    public long getSinceId() {
        return this.sinceId;
    }

    /**
     * returns id of the last tweet to scrape,
     * or a negative number if no upper limit should be set
     * @return
     */
    public long getMaxId() {
        return this.maxId;
    }
}
