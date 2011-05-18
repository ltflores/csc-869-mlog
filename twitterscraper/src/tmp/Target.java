package tmp;

/**
 *
 * @author orensf
 */
public enum Target {
    // list targets in order of the work flow:
    ScrapeTweetInterval("scrape-tweet-interval"),
    ScrapeTimeline("scrape-timeline"),
    ScrapeFriends("scrape-friends"),
    ScrapeDictionary("scrape-dictionary"),
    BuildDirectory("build-directory"),
    Tokenize("tokenize"),
    // All has to be the last target:
    All("all");

    private String name;

    private Target(String name) {
        this.name = name;
    }

    /**
     * converts given string to Target.
     * returns null if requested target cannot be found.
     * @param argument
     * @return
     */
    public static Target getTarget(String argument) {
        for (Target target : Target.values()) {
            if (target.name.equals(argument)) {
                return target;
            }
        }
        // else, requested target was not found
        return null;
    }

    public String getName() {
        return this.name;
    }
}
