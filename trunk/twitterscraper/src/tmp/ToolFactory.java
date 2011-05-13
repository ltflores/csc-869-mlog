package tmp;

import tmp.twitter.FriendScraper;
import tmp.splitters.WordSplitter;
import tmp.splitters.IStringSplitter;
import tmp.twitter.IScraper;

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
