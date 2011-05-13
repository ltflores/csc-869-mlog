package main;

import twitter.FriendScraper;
import splitters.WordSplitter;
import splitters.IStringSplitter;
import twitter.IScraper;

/**
 *
 * @author orensf
 */
public class ToolFactory {
    private Arguments arguments;

    public ToolFactory(Arguments arguments) {
        this.arguments = arguments;
    }


    public IScraper getScraper() {
        switch (arguments.getTarget()) {
            case ScrapeFriends:
                return new FriendScraper();
            default:
                Logger.log(Logger.Level.ERROR, "could not find "
                        + "a scraper tool for target: "
                        + arguments.getTarget().getName());
                return null;
        }

    }

    public IStringSplitter getWordSplitter() {
        return new WordSplitter();
    }



}
