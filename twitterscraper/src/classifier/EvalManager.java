package classifier;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import weka.core.Instances;

import java.io.File;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
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

public class EvalManager {
	
	
	private String outputfilename = "out.csv";
	
	public EvalManager(){
	}
		
	
	
	/*
	 * Scripts are in comma delimited file format 
	 * each line has the following options
	 * 
	 *  
	 */
	private Vector<EvalRecord> loadevalscript(String script)
	{
		// just call the handy CsvReader 
		try {
			
			Vector<EvalRecord> scriptParams = new Vector<EvalRecord>();
			CsvReader csv = new CsvReader(script);
			while (csv.readRecord())
			{
				EvalRecord next = new EvalRecord(); 
				
				// read params
				next.setIn(csv.get("in"));
				next.setClassifier(TweetClassifier.ClassifierType.valueOf(csv.get("classifier")));
				next.setLoader(TweetClassifier.LoaderType.valueOf(csv.get("loader")));
				next.setTokenizer(TweetClassifier.TokenizerType.valueOf(csv.get("tokenizer")));
				next.setStemmer(TweetClassifier.StemmerType.valueOf(csv.get("stemmer")));
				next.setK(Integer.parseInt(csv.get("k")));
				next.setFeatures(Integer.parseInt(csv.get("features")));
				next.setNgrammin(Integer.parseInt(csv.get("ngrammin")));
				next.setStopwords(Boolean.parseBoolean(csv.get("stopwords")));
				next.setEvaluate(Boolean.parseBoolean(csv.get("evaluate")));
				next.setModelFileName(csv.get("modelfilename"));
				next.setSave(Boolean.parseBoolean(csv.get("save")));
				
				// read results
				String tmp;
				tmp = csv.get("totalinstances");
				if (tmp.length()==0) tmp = "0";
				next.setTotalInstances(Integer.parseInt(tmp));
				tmp = csv.get("corclassified");
				if (tmp.length()==0) tmp = "0";
				next.setCorclassified(Integer.parseInt(tmp));
				tmp = csv.get("incclassified");
				if (tmp.length()==0) tmp = "0";
				next.setIncclassified(Integer.parseInt(tmp));
				tmp = csv.get("kappa");
				if (tmp.length()==0) tmp = "0";
				next.setKappa(Float.parseFloat(tmp));
				tmp = csv.get("meanabserr");
				if (tmp.length()==0) tmp = "0";
				next.setMeanabserr(Float.parseFloat(tmp));
				tmp = csv.get("rmserr");
				if (tmp.length()==0) tmp = "0";
				next.setRmserr(Float.parseFloat(tmp));
				tmp = csv.get("relabserr");
				if (tmp.length()==0) tmp = "0";
				next.setRelabserr(Float.parseFloat(tmp));
				tmp = csv.get("rootrelsqerr");
				if (tmp.length()==0) tmp = "0";
				next.setRootrelsqerr(Float.parseFloat(tmp));
				tmp = csv.get("coverage");
				if (tmp.length()==0) tmp = "0";
				next.setCoverage(Float.parseFloat(tmp));
				tmp = csv.get("meanrelreg");
				if (tmp.length()==0) tmp = "0";
				next.setMeanrelreg(Float.parseFloat(tmp));
				tmp = csv.get("precision");
				if (tmp.length()==0) tmp = "0";
				next.setPrecision(Double.parseDouble(tmp));
				tmp = csv.get("pctcorrect");
				if (tmp.length()==0) tmp = "0";
				next.setPctcorrect(Double.parseDouble(tmp));
				tmp = csv.get("pctincorrect");
				if (tmp.length()==0) tmp = "0";
				next.setPctincorrect(Double.parseDouble(tmp));
				tmp = csv.get("pctunclassified");
				if (tmp.length()==0) tmp = "0";
				next.setPctunclassified(Double.parseDouble(tmp));
				tmp = csv.get("tp");
				if (tmp.length()==0) tmp = "0";
				next.setTp(Integer.parseInt(tmp));
				tmp = csv.get("tn");
				if (tmp.length()==0) tmp = "0";
				next.setTn(Integer.parseInt(tmp));
				tmp = csv.get("fp");
				if (tmp.length()==0) tmp = "0";
				next.setFp(Integer.parseInt(tmp));
				tmp = csv.get("fn");
				if (tmp.length()==0) tmp = "0";
				next.setFn(Integer.parseInt(tmp));
				
				
				// add to the list
				scriptParams.add(next);
			}
			csv.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public void writeEvalResults(String outputFile, Vector<EvalRecord> results){
		
		// before we open the file check to see if it already exists
		boolean alreadyExists = new File(outputFile).exists();
			
		try {
			// use FileWriter constructor that specifies open for appending
			CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');
			
			// if the file didn't already exist then we need to write out the header line
			if (!alreadyExists)
			{
				writeHeader(csvOutput);
			}
			
			Iterator<EvalRecord> it = results.iterator();
			
			while (it.hasNext()){
				EvalRecord cur = it.next();
				
				// Results - in same order as they are declared in EvalParams
				
				csvOutput.write(cur.getIn());
				csvOutput.write(cur.getClassifier().toString());
				csvOutput.write(cur.getLoader().toString());
				csvOutput.write(cur.getTokenizer().toString());
				csvOutput.write(cur.getStemmer().toString());
				csvOutput.write(Integer.toString(cur.getK()));
				csvOutput.write(Integer.toString(cur.getFeatures()));
				csvOutput.write(Integer.toString(cur.getNgrammin()));
				csvOutput.write(Boolean.toString(cur.isStopwords()));
				csvOutput.write(Boolean.toString(cur.isEvaluate()));
				csvOutput.write(cur.getModelFileName());
				csvOutput.write(Boolean.toString(cur.isSave()));
				
				// Results - in same order as they are declared in EvalParams
				csvOutput.write(Integer.toString(cur.getTotalInstances()));
				csvOutput.write(Integer.toString(cur.getCorclassified()));
				csvOutput.write(Integer.toString(cur.getIncclassified()));
				csvOutput.write(Float.toString(cur.getKappa()));
				csvOutput.write(Float.toString(cur.getMeanabserr()));
				csvOutput.write(Double.toString(cur.getRmserr()));
				csvOutput.write(Double.toString(cur.getRelabserr()));
				csvOutput.write(Double.toString(cur.getRootrelsqerr()));
				csvOutput.write(Float.toString(cur.getCoverage()));
				csvOutput.write(Float.toString(cur.getMeanrelreg()));
				csvOutput.write(Double.toString(cur.getPrecision()));
				csvOutput.write(Double.toString(cur.getPctcorrect()));
				csvOutput.write(Double.toString(cur.getPctincorrect()));
				csvOutput.write(Double.toString(cur.getPctunclassified()));
				csvOutput.write(Double.toString(cur.getTp()));
				csvOutput.write(Double.toString(cur.getTn()));
				csvOutput.write(Integer.toString(cur.getFp()));
				csvOutput.write(Integer.toString(cur.getFn()));
			}
			csvOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
	}
	
	private void writeHeader(CsvWriter csvOutput) {
		
		
		String[] header = { "in","classifier","loader","tokenizer","stemmer","k","features",
				"ngrammin",	"stopwords","evaluate", "save",
				// Output results
				"totalInstances","corclassified","incclassified","kappa","meanabserr","rmserr",
				"relabserr","rootrelsqerr","coverage","meanrelreg","precision","pctcorrect",
				"pctincorrect","pctunclassified","tp","tn","fp","fn"};
		
			
		
			try {
				
				for(String field  : header)
				{
					csvOutput.write(field);
				}
				
				csvOutput.endRecord();
					
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}


	private boolean eval(EvalRecord testcase){
		
		TweetClassifier tweetclass = new TweetClassifier();
        
		
		Instances tweets = tweetclass.loadTweets(testcase.getIn(), 
				testcase.getLoader(), testcase.getTokenizer(), testcase.getStemmer(),
				testcase.isStopwords(), testcase.getFeatures(), testcase.getNgrammin());
        
        if (testcase.isEvaluate()){
        	tweetclass.evaluateClassifier(tweets, testcase.getClassifier(), testcase.getK());
        } else {
        	System.out.println("Not evaluating");
        }
        
        if(testcase.getModelFileName() == null) {
    		System.out.println("Not saving model");
    	} else {
    		System.out.println("Using -save=" + testcase.getModelFileName());
    		tweetclass.saveClassifier(tweets, testcase.getClassifier(), testcase.getModelFileName());
    	}
        
        // now save the eval results to the params.
        Evaluation eval = tweetclass.getEval();
        // store the evaluation results in the testcase
        testcase.readEval(eval);
        return true;
		
	}
	
	
	public static void main(String[] args) throws Exception {
		try{
			
			String input = "";
			String output = "output.csv";
			for(String arg : args){
				
				if(arg.startsWith("-input=")) {
					input = arg.substring("-input=".length());
				} else if(arg.startsWith("-output=")) {
					output = arg.substring("-output=".length());
				}
			}
				
			EvalManager manager = new EvalManager();
			if (input.length()!=0){
				manager.evaluate(input, output);
			} else {
				// guess we are just writing a header.
				manager.writeEmptyFile(output);
			}
			
		}catch(Exception ex){
			throw ex;
		}
	}
	
	// should just write us an empty file
	public void writeEmptyFile(String filename)
	{
		writeEvalResults(filename, new Vector<EvalRecord>());
	}
	
	
	public void evaluate(String scriptfilename, String outputfilename){
		
		Vector<EvalRecord> testcases = loadevalscript(scriptfilename);
		
		Iterator<EvalRecord> it = testcases.iterator();
		while(it.hasNext())
		{
			eval(it.next());
		}
		// now they are all done, lets save the results
		
		writeEvalResults(outputfilename, testcases);
		
		
		
		
	}
	

	

}
