package classifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import weka.classifiers.Evaluation;

public class EvalManager {

	private String outputfilename = "out.csv";

	public EvalManager(String outname) {
		outputfilename = outname;
	}

	public void appendevalresult(String results) {
		try {
			// Create file
			FileWriter fstream = new FileWriter("out.txt", true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(results);
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	/*
	 * Scripts are in comma delimited file format each line has the following
	 * options
	 * 
	 * These are in the following format classifier options, comma delimited. in
	 * k classifier loader tokenizer debug nostopwords stemmer save noeval
	 * features ngrammin
	 */
	private Vector<EvalRecord> loadevalscript(String script) {
		// just call the handy CsvReader
		try {

			Vector<EvalRecord> scriptParams = new Vector<EvalRecord>();
			CsvReader csv = new CsvReader(script);
			while (csv.readRecord()) {
				EvalRecord next = new EvalRecord();
				// read params
				next.setClassifier(TweetClassifier.ClassifierType.valueOf(csv.get("classifier")));
				next.setLoader(TweetClassifier.LoaderType.valueOf(csv.get("loader")));
				next.setStemmer(TweetClassifier.StemmerType.valueOf(csv.get("stemmer")));
				next.setLoader(TweetClassifier.LoaderType.valueOf(csv.get("loader")));
				next.setIn(csv.get("in"));
				next.setK(Integer.parseInt(csv.get("k")));
				next.setStopwords(Boolean.parseBoolean(csv.get("nostopwords")));
				next.setEvaluate(Boolean.parseBoolean(csv.get("noeval")));
				next.setSave(Boolean.parseBoolean(csv.get("save")));
				next.setFeatures(Integer.parseInt(csv.get("features")));

				// read results
				String tmp;
				tmp = csv.get("totalinstances");
				if (tmp.length() == 0)
					tmp = "0";
				next.setTotalInstances(Integer.parseInt(tmp));
				tmp = csv.get("corclassified");
				if (tmp.length() == 0)
					tmp = "0";
				next.setCorclassified(Integer.parseInt(tmp));
				tmp = csv.get("incclassified");
				if (tmp.length() == 0)
					tmp = "0";
				next.setIncclassified(Integer.parseInt(tmp));
				tmp = csv.get("kappa");
				if (tmp.length() == 0)
					tmp = "0";
				next.setKappa(Float.parseFloat(tmp));
				tmp = csv.get("meanabserr");
				if (tmp.length() == 0)
					tmp = "0";
				next.setMeanabserr(Float.parseFloat(tmp));
				tmp = csv.get("rmserr");
				if (tmp.length() == 0)
					tmp = "0";
				next.setRmserr(Float.parseFloat(tmp));
				tmp = csv.get("relabserr");
				if (tmp.length() == 0)
					tmp = "0";
				next.setRelabserr(Float.parseFloat(tmp));
				tmp = csv.get("rootrelsqerr");
				if (tmp.length() == 0)
					tmp = "0";
				next.setRootrelsqerr(Float.parseFloat(tmp));
				tmp = csv.get("coverage");
				if (tmp.length() == 0)
					tmp = "0";
				next.setCoverage(Float.parseFloat(tmp));
				tmp = csv.get("meanrelreg");
				if (tmp.length() == 0)
					tmp = "0";
				next.setMeanrelreg(Float.parseFloat(tmp));
				tmp = csv.get("tp");
				if (tmp.length() == 0)
					tmp = "0";
				next.setTp(Integer.parseInt(tmp));
				tmp = csv.get("tn");
				if (tmp.length() == 0)
					tmp = "0";
				next.setTn(Integer.parseInt(tmp));
				tmp = csv.get("fp");
				if (tmp.length() == 0)
					tmp = "0";
				next.setFp(Integer.parseInt(tmp));
				tmp = csv.get("fn");
				if (tmp.length() == 0)
					tmp = "0";
				next.setFn(Integer.parseInt(tmp));
				tmp = csv.get("precision");
				if (tmp.length() == 0)
					tmp = "0";
				next.setPrecision(Double.parseDouble(tmp));
				tmp = csv.get("pctcorrect");
				if (tmp.length() == 0)
					tmp = "0";
				next.setPctcorrect(Double.parseDouble(tmp));
				tmp = csv.get("pctincorrect");
				if (tmp.length() == 0)
					tmp = "0";
				next.setPctincorrect(Double.parseDouble(tmp));
				tmp = csv.get("pctunclassified");
				if (tmp.length() == 0)
					tmp = "0";
				next.setPctunclassified(Double.parseDouble(tmp));

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

	public void writeEvalResults(String outputFile, Vector<EvalRecord> results) {

		// before we open the file check to see if it already exists
		boolean alreadyExists = new File(outputFile).exists();

		try {
			// use FileWriter constructor that specifies open for appending
			CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');

			// if the file didn't already exist then we need to write out the
			// header line
			if (!alreadyExists) {
				writeHeader(csvOutput);
			}
			
			Iterator<EvalRecord> it = results.iterator();
			while (it.hasNext()) {
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
		String[] header = { "in", "classifier", "loader", "tokenizer", "stemmer", "k", "features", "ngrammin",
				"stopwords", "evaluate", "modelFileName",
				"save",
				// Output results
				"totalInstances", "corclassified", "incclassified", "kappa", "meanabserr", "rmserr", "relabserr",
				"rootrelsqerr", "coverage", "meanrelreg", "precision", "pctcorrect", "pctincorrect", "pctunclassified",
				"tp", "tn", "fp", "fn" };
		try {

			for (String field : header) {
				csvOutput.write(field);
			}

			csvOutput.endRecord();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void eval(EvalRecord params) {
		List<String> args = new ArrayList<String>();
		args.add("-in=" + params.getIn());
		args.add("-loader=" + params.getLoader());
		args.add("-tokenizer=" + params.getTokenizer());
		args.add("-stemmer=" + params.getStemmer());
		args.add("-features=" + params.getFeatures());
		args.add("-ngrammin=" + params.getNgrammin());
		args.add("-classifier=" + params.getClassifier());
		args.add("-k=" + params.getK());
		if (!params.isStopwords()) {
			args.add("-nostopwords");
		}
		if (params.getModelFileName() != null) {
			args.add("-save=" + params.getModelFileName());
		}

		TweetClassifier tweetclass = new TweetClassifier(args.toArray(new String[] {}));
		tweetclass.runClassification();

		// now save the eval results to the params.
		Evaluation eval = tweetclass.getEval();
		params.readEval(eval);
	}

}
