/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package classifier;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.stemmers.SnowballStemmer;
import weka.core.tokenizers.NGramTokenizer;
import weka.core.tokenizers.Tokenizer;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * 
 * @author Gavin
 */
public class TweetClassifier {

	private static final Logger logger = Logger.getLogger(TweetClassifier.class.getName());

	public static boolean debug = false;

	enum ClassifierType {
		BAYES, J48
	}

	enum LoaderType {
		SENTENCE, FILE
	}

	enum TokenizerType {
		WORD, NGRAM
	}

	enum StemmerType {
		NONE, SNOWBALL
	}

	// Arguments
	private String in = null;
	private String outputModel = null; 
	private String inputModel = null;
	private ClassifierType classifierType = null;
	private LoaderType loaderType = null;
	private TokenizerType tokenizerType = null;
	private StemmerType stemmerType = null;
	private int ngrammin = -1;
	private int k = 10;
	private int features = 1000;
	private boolean stopwords = true;
	private boolean noeval = false;
	
	private Evaluation eval;

	public static void main(String[] args) throws Exception {
		TweetClassifier tweetclass = new TweetClassifier(args);
		tweetclass.runClassification();
	}

	public TweetClassifier(String[] args) {
		parseArguments(args);
		validateArguments();
	}

	private void parseArguments(String[] args) {
		for (String arg : args) {
			if (arg.startsWith("-in=")) {
				in = arg.substring("-in=".length());
			} else if (arg.startsWith("-k=")) {
				k = Integer.parseInt(arg.substring("-k=".length()));
			} else if (arg.startsWith("-classifier=")) {
				classifierType = ClassifierType.valueOf(arg.substring("-classifier=".length()));
			} else if (arg.startsWith("-loader=")) {
				loaderType = LoaderType.valueOf(arg.substring("-loader=".length()));
			} else if (arg.startsWith("-tokenizer=")) {
				tokenizerType = TokenizerType.valueOf(arg.substring("-tokenizer=".length()));
			} else if (arg.equals("-debug")) {
				debug = true;
			} else if (arg.equals("-nostopwords")) {
				stopwords = false;
			} else if (arg.startsWith("-stemmer=")) {
				stemmerType = StemmerType.valueOf(arg.substring("-stemmer=".length()));
			} else if (arg.startsWith("-save=")) {
				outputModel = arg.substring("-save=".length());
			} else if (arg.startsWith("-features=")) {
				features = Integer.parseInt(arg.substring("-features=".length()));
			} else if (arg.startsWith("-model=")) {
				inputModel = arg.substring("-model=".length());
			} else if (arg.startsWith("-noeval")) {
				noeval = true;
			} else if(arg.startsWith("-ngrammin=")){
                ngrammin = Integer.parseInt(arg.substring("-ngrammin=".length()));
            }

		}
	}

	private void validateArguments() {
		if (in == null) {
			in = "data";
		}
		System.out.println("Using -in=" + in);

		if (inputModel != null && outputModel != null) {
			logger.severe("Using can't use -save and -model at the same time!");
			System.exit(-1);
		}

		if (classifierType == null) {
			classifierType = ClassifierType.BAYES;
		} else if (classifierType.equals("J48")) {
			classifierType = ClassifierType.J48;
		}
		System.out.println("Using -classifier=" + classifierType);

		if (loaderType == null) {
			loaderType = LoaderType.FILE;
		}
		System.out.println("Using -loader=" + loaderType);

		if (tokenizerType == null) {
			tokenizerType = TokenizerType.WORD;
		}
		System.out.println("Using -tokenizer=" + tokenizerType);

		if (stopwords) {
			System.out.println("Using stopword removal");
		}

		if (stemmerType == null) {
			stemmerType = StemmerType.NONE;
		}
		System.out.println("Using -stemmer=" + stemmerType);
		System.out.println("Using -k=" + k);
		System.out.println("Using -features=" + features);
	}

	public void runClassification() {
		Instances tweets = loadTweets();
		
		if(inputModel!=null) {
			AbstractClassifier cls = null;
			try {
				cls = (AbstractClassifier) weka.core.SerializationHelper.read(inputModel);
			} catch (Exception e) {
				e.printStackTrace();
			}
			evaluateClassifier(tweets, cls);
		} else {
			Classifier cls = crossvalidateClassifier(tweets);
			if(outputModel!=null) {
				try {
					weka.core.SerializationHelper.write(outputModel, cls);
					System.out.println("Using -save=" + outputModel);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Not saving model");
			}
		}
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
	private Instances loadTweets() {
		try {
			Instances dataRaw = loadRawData();
			Instances dataSentences = transformData(dataRaw);

			if (debug) {
				for (Instance inst : dataSentences) {
					System.out.println(inst.classAttribute().value(inst.classIndex()));
				}
				System.out.println(dataSentences.toString());
			}
			dataSentences.randomize(new Random(29390));
			return dataSentences;
		} catch (Exception ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		return null;
	}

	private Instances loadRawData() throws IOException {
		CustomTextDirectoryLoader loader = null;
		switch (loaderType) {
		case SENTENCE:
			loader = new SentenceBasedTextDirectoryLoader();
			break;
		case FILE:
			// loader = new TextDirectoryLoader();
			loader = new CustomTextDirectoryLoader();
			break;
		}
		loader.setOutputFilename(true);
		loader.setDirectory(new File(in));

		Instances dataRaw = loader.getDataSet();
		if (debug) {
			System.out.println(dataRaw.toString());
		}
		return dataRaw;
	}

	private Instances transformData(Instances dataRaw) throws Exception {
		StringToWordVector filter = new StringToWordVector();
		filter.setInputFormat(dataRaw);
		filter.setLowerCaseTokens(true);

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
		if (stemmerType == StemmerType.SNOWBALL) {
			filter.setStemmer(new SnowballStemmer());
		}

		// set the stop words file.
		filter.setUseStoplist(stopwords);

		// set feature space size
		filter.setWordsToKeep(features);
		filter.setAttributeIndicesArray(new int[] { 0 });

		Instances dataSentences = Filter.useFilter(dataRaw, filter);
		return dataSentences;
	}

	private Classifier crossvalidateClassifier(Instances data) {
		try {
			AbstractClassifier classifier = null;
			switch (classifierType) {
				case BAYES:
					classifier = new NaiveBayes();
					break;
				case J48:
					classifier = new J48();
			}

			classifier.setDebug(true);

			// Create remove file name filter
			Remove remove = new Remove();
			remove.setAttributeIndicesArray(new int[] { 0 });
			remove.setInputFormat(data);

			FilteredClassifier filtered = new FilteredClassifier();
			filtered.setClassifier(classifier);
			filtered.setFilter(remove);

			if(!noeval) {
				this.eval = new Evaluation(data);
				// perform cross-validation
				for (int n = 0; n < k; n++) {
					Instances train = data.trainCV(k, n);
					Instances test = data.testCV(k, n);
	
					// build and evaluate classifier
					Classifier clsCopy = AbstractClassifier.makeCopy(filtered);
					clsCopy.buildClassifier(train);
					eval.evaluateModel(clsCopy, test);
				}
				outputStatistics(data, classifier, eval, true);
			} else {
				System.out.println("Not evaluating");
			}
			
			filtered.buildClassifier(data);
			return filtered;
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
		return null;
	}
	
	private void evaluateClassifier(Instances data, AbstractClassifier cls) {
		try {
			Evaluation eval = new Evaluation(data);
			eval.evaluateModel(cls, data);
			outputStatistics(data, cls, eval, false);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
	}

	private void outputStatistics(Instances data, AbstractClassifier classifier, Evaluation eval, boolean isCrossvalidation) {
		// output evaluation
		System.out.println();
		System.out.println("=== Setup ===");
		System.out.println("Classifier: " + classifier.getClass().getName()
		                + " " + Utils.joinOptions(classifier.getOptions()));
		System.out.println("Dataset: " + data.relationName());
		if(isCrossvalidation) {
			System.out.println("Folds: " + k);
			System.out.println();
			System.out.println(eval.toSummaryString("=== " + k
			                + "-fold Cross-validation ===", false));
		} else {
			System.out.println();
			System.out.println(eval.toSummaryString("=== run on validation set ===", false));
		}
		
		// Check which instances were falsely classified
		FastVector predictions = eval.predictions();
		for (int i = 0; i < predictions.size(); i++) {
		        NominalPrediction prediction = (NominalPrediction) predictions.get(i);
		        if(prediction.actual()!=prediction.predicted()) {
		                System.out.println("Falsely classified " + data.get(i).stringValue(0));
		        }
		}
	}
	
	public Evaluation getEval() {
		return this.eval;
	}

}