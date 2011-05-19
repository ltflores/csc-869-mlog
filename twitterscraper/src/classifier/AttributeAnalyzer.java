package classifier;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.ErrorBasedMeritEvaluator;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.OneRAttributeEval;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.SymmetricalUncertAttributeEval;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.stemmers.SnowballStemmer;
import weka.core.tokenizers.NGramTokenizer;
import weka.core.tokenizers.Tokenizer;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;
import classifier.TweetClassifier.ClassifierType;
import classifier.TweetClassifier.LoaderType;
import classifier.TweetClassifier.StemmerType;
import classifier.TweetClassifier.TokenizerType;


public class AttributeAnalyzer {


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
	
	weka.attributeSelection.AttributeSelection attSelect = null;
	private String results;
	private Instances currentRankedInstances;
	
	public AttributeAnalyzer(String[] args) {
		parseArguments(args);
		validateArguments();
	}

	private void validateArguments() {
		
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
            } else if (arg.startsWith("-results=")) {
				results = arg.substring("-results=".length());
            } else {
            	System.out.println("Unknown command line arument: " + arg);
            	System.exit(-1);
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
	
	private Instances loadRawData() throws IOException {
		CustomTextDirectoryLoader loader = null;
		loader = new CustomTextDirectoryLoader();
		loader.setOutputFilename(true);
		loader.setDirectory(new File(in));

		Instances dataRaw = loader.getDataSet();
		if (debug) {
			System.out.println(dataRaw.toString());
		}
		return dataRaw;
	}
	
	static String stripExtension (String str) {         
		if (str == null)             
			return null;         
		int pos = str.lastIndexOf(".");         
		if (pos == -1)             
			return str;         
		return str.substring(0,pos);     
	} 
	
	public void writeTopResults(int rankMax, String outputFile)
	{
		// before we open the file check to see if it already exists
		boolean alreadyExists = new File(outputFile).exists();

		try {
			// use FileWriter constructor that specifies open for appending
			CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');
			double[][] ranked = attSelect.rankedAttributes();
			// go through the ranked attributes and get the name from the instances
			
			int totalAttributes = 0;
			csvOutput.write(in);
			for (int i = 0;i<rankMax; i++)
			{
				String s = currentRankedInstances.attribute((int)ranked[i][0]).name();
				csvOutput.write(s);
				//csvOutput.endRecord();
			}
			csvOutput.endRecord();		
			//String[] arr = toResultsString().split("\n");
			
			// Results - in same order as they are declared in EvalParams
			//csvOutput.write(in);
			
			//for (String str : arr){
			//	csvOutput.write(str);
				//csvOutput.endRecord();
			//}
			csvOutput.close();
		}
		catch( Exception ex)
		{
			
		}
	}
			
	private void selectAttributes(Instances dataRaw) {
		
		StringToWordVector filter = new StringToWordVector();
		
		try {
			filter.setInputFormat(dataRaw);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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

		
		try {
			Instances dataSentences = Filter.useFilter(dataRaw, filter);
			
			Remove remove = new Remove();
			remove.setAttributeIndicesArray(new int[] { 0 });
			remove.setInputFormat(dataSentences);
			remove.setInputFormat(dataSentences);
		    currentRankedInstances = Filter.useFilter(dataSentences, remove);
			
		    // all these evaluators require a string class attribute (#1 
		    //weka.attributeSelection.CfsSubsetEval eval = new CfsSubsetEval();
		    //weka.attributeSelection.GainRatioAttributeEval eval = new GainRatioAttributeEval();
		    weka.attributeSelection.SymmetricalUncertAttributeEval eval = new SymmetricalUncertAttributeEval();
		    
		    attSelect = new AttributeSelection();
			attSelect.setEvaluator(eval);
			attSelect.setFolds(10);
			attSelect.setRanking(true);
			attSelect.setXval(true);
			attSelect.setSeed(11299404);
			//dataRaw = attSelect.reduceDimensionality(dataRaw);
			weka.attributeSelection.Ranker rankSearch = new Ranker();
			attSelect.setSearch(rankSearch);

			attSelect.SelectAttributes(currentRankedInstances);
			
			System.out.println(attSelect.toResultsString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return selectedAttributes;
		
	}

	public void runAnalysis() {
		// TODO Auto-generated method stub
		try {
			Instances dataRaw = loadRawData();
			selectAttributes(dataRaw);
			
			writeTopResults(50, results);
						
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}
