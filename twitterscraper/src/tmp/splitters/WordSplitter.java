package tmp.splitters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 *
 * @author orensf
 */
public class WordSplitter implements IStringSplitter{
    private Pattern spacePattern;
    private Pattern wordPattern;

    public WordSplitter() {
        this.wordPattern = Pattern.compile("\\w+");
        this.spacePattern = Pattern.compile("\\s+");
    }

    public void setMultiString(boolean multiString) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] split(String textToSplit) {
        Collection<String> result = new ArrayList<String>();
        for (String token : textToSplit.split("\\s+")) {
            StringBuilder wordBuilder = new StringBuilder();
            for (String wordPart : token.split("\\W+")) {
                wordBuilder.append(wordPart);
            }
            String wordCandidate = wordBuilder.toString().trim().toLowerCase();
            if (!wordCandidate.isEmpty()) {
                result.add(wordCandidate);
            }
        }
        return result.toArray(new String[0]);
    }

}
