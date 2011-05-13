package main;

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
import main.Logger;
import main.Logger.Level;
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
    private static final String ARG_SCREEN_NAME_PATH = "--screen-name-path";
    private static final String ARG_SCREEN_NAME = "--screen-name=";
    private static final String ARG_SINCE_DATE = "--since-date=";
    private static final String ARG_UNTIL_DATE = "--until-date=";


    // defaults:
    private Set<String> dataFileNames = new HashSet<String>();
    private Set<String> screenNames = new HashSet<String>();
    private Target target = Target.All;
    private Logger.Level logLevel = Logger.Level.ERROR;
    private DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private Date sinceDate = new Date(0L); // the big bang
    private Date untilDate = new Date(); // now

    
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
                // for (String filename : this.parsePathList(paths)) {System.out.println(filename);}
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

    public Collection<String> getScreenNames() {
        return this.screenNames;
    }

    public Date getSinceDate() {
        return this.sinceDate;
    }

    public Date getUntilDate() {
        return this.untilDate;
    }
}
