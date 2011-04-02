/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package classifier;

import java.io.File;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.TextDirectoryLoader;
import weka.core.stemmers.SnowballStemmer;
import weka.core.tokenizers.NGramTokenizer;
import weka.core.tokenizers.Tokenizer;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 *
 * @author Gavin
 */
public class TweetClassifier {
	
	private final Logger logger = Logger.getLogger(TweetClassifier.class.getName());
	
	public static boolean debug = false;
	
	private enum ClassifierType {
	    BAYES, J48
	}
	
	private enum LoaderType {
	    SENTENCE, FILE
	}
	
	private enum TokenizerType {
	    WORD, NGRAM
	}
	
	private enum StemmerType {
	    NONE, SNOWBALL
	}


    public static void main(String[] args) throws Exception {
    	String in = null;
    	ClassifierType classifier = null;
    	LoaderType loader = null;
    	TokenizerType tokenizer = null;
    	StemmerType stemmer = null;
    	int k = 10;
    	boolean stopwords = true;
    	
    	for(String arg : args) {
    		if(arg.startsWith("-in=")) {
    			in = arg.substring("-in=".length());
    		} else if(arg.startsWith("-k=")) {
    			k = Integer.parseInt(arg.substring("-k=".length()));
    		} else if(arg.startsWith("-classifier=")) {
    			classifier = ClassifierType.valueOf(arg.substring("-classifier=".length()));
    		} else if(arg.startsWith("-loader=")) {
    			loader = LoaderType.valueOf(arg.substring("-loader=".length()));
    		} else if(arg.startsWith("-tokenizer=")) {
    			tokenizer = TokenizerType.valueOf(arg.substring("-tokenizer=".length()));
    		} else if(arg.equals("-debug")) {
    			debug = true;
    		} else if(arg.equals("-nostopwords")) {
    			stopwords = false;
    		} else if(arg.startsWith("-stemmer=")) {
    			stemmer = StemmerType.valueOf(arg.substring("-stemmer=".length()));
    		}
    	}
    	
    	if(in==null) {
    		in = "data"; 
    	}
    	System.out.println("Using -in=" + in);
    	
    	if(classifier == null) {
    		classifier = ClassifierType.BAYES;
    	} else if (classifier.equals("J48")){
    		classifier = ClassifierType.J48;
    	}
    	System.out.println("Using -classifier=" + classifier);
    	
    	if(loader == null) {
    		loader = LoaderType.SENTENCE;
    	}
    	System.out.println("Using -loader=" + loader);
    	
    	if(tokenizer == null) {
    		tokenizer = TokenizerType.WORD;
    	}
    	System.out.println("Using -tokenizer=" + tokenizer);
    	
    	if(stopwords) {
    		System.out.println("Using stopword removal");
    	}
    	
    	if(stemmer == null) {
    		stemmer = StemmerType.NONE;
    	}
    	System.out.println("Using -stemmer=" + stemmer);
    	

        TweetClassifier tweetclass = new TweetClassifier();
        Instances tweets = tweetclass.loadTweets(in, loader, tokenizer, stemmer, stopwords);
        tweetclass.evaluateClassifier(tweets, classifier, k);

    }

    /*
     * expects data like so
     *
     * directoryPath/
     *          |
     *          |-republican-|
     *          |            |- bush.txt
     *          |            |- regan.txt
     *          |
     *          |-democrat---|
     *          |            |- obama.txt
     *          |            |- clinton.txt
     *
     * Creates an Instances using TextDirectoryLoader, with each of the
     */
	public Instances loadTweets(String directoryPath, LoaderType loaderType,
			TokenizerType tokenizerType, StemmerType stemmer, boolean stopwords) {
		try {
			TextDirectoryLoader loader = null;
			switch (loaderType) {
			case SENTENCE:
				loader = new SentenceBasedTextDirectoryLoader();
				break;
			case FILE:
				loader = new TextDirectoryLoader();
				break;
			}

			File dir = new File(directoryPath);
			Instances dataRaw;
			// make sure we remember where the attribute came from
			loader.setOutputFilename(true);
			loader.setDirectory(dir);

			dataRaw = loader.getDataSet();
			if (debug) {
				System.out.println(dataRaw);
			}

			StringToWordVector filter = new StringToWordVector();
			filter.setInputFormat(dataRaw);

			// set the tokenizer
			Tokenizer tokenizer = null;
			switch (tokenizerType) {
			case WORD:
				tokenizer = new WordTokenizer();
				break;
			case NGRAM:
				tokenizer = new NGramTokenizer();
				break;
			}
			filter.setTokenizer(tokenizer);

			// set stemmer
			if (stemmer == StemmerType.SNOWBALL) {
				filter.setStemmer(new SnowballStemmer());
			}

			// set the stop words file.
			filter.setUseStoplist(stopwords);

			Instances dataSentences = Filter.useFilter(dataRaw, filter);
			filter.setInputFormat(dataSentences);
			Instances dataSentenceAndWordTokenized = Filter.useFilter(
					dataSentences, filter);

			for (Instance inst : dataSentenceAndWordTokenized) {
				if (debug) {
					System.out.println(inst.classAttribute().value(
							inst.classIndex()));
				}
			}

			if (debug) {
				System.out.println(dataSentenceAndWordTokenized);
			}

			// dataBOW.stratify(k);
			int seed = 29390;

			Random rand = new Random(seed); // create seeded number generator
			Instances randData = new Instances(dataSentenceAndWordTokenized); // create
																				// copy
																				// of
																				// original
																				// data
			randData.randomize(rand); // randomize data with number generator

			return randData;
		} catch (Exception ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		return null;
	}
            
            
	public void evaluateClassifier(Instances data,
			ClassifierType classifierType, int k) {
		try {
			AbstractClassifier classifier = null;
			switch (classifierType) {
			case BAYES:
				classifier = new NaiveBayes();
				break;
			case J48:
				classifier = new J48();
			}
			Evaluation eval = new Evaluation(data);
			for (int n = 0; n < k; n++) {
				Instances train = data.trainCV(k, n);
				Instances test = data.testCV(k, n);
				// the above code is used by the StratifiedRemoveFolds filter,
				// the
				// code below by the Explorer/Experimenter:
				// Instances train = randData.trainCV(folds, n, rand);

				// build and evaluate classifier
				Classifier copy = AbstractClassifier.makeCopy(classifier);
				copy.buildClassifier(train);
				eval.evaluateModel(copy, test);
			}
			// nbayes.buildClassifier(dataRaw);

			// output evaluation
			System.out.println();
			System.out.println("=== Setup ===");
			System.out.println("Classifier: " + classifier.getClass().getName()
					+ " " + Utils.joinOptions(classifier.getOptions()));
			System.out.println("Dataset: " + data.relationName());
			System.out.println("Folds: " + k);
			System.out.println();
			System.out.println(eval.toSummaryString("=== " + k
					+ "-fold Cross-validation ===", false));
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
	}

}