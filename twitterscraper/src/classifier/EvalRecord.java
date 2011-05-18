package classifier;

import weka.classifiers.Evaluation;


/*
 * Gavin Dodd 
 * Created 5/16/2011
 */
public class EvalRecord {

	
	// input parameters
	
	private String in = "data";
	private TweetClassifier.ClassifierType classifier = TweetClassifier.ClassifierType.BAYES;
	private TweetClassifier.LoaderType loader = TweetClassifier.LoaderType.FILE;
	private TweetClassifier.TokenizerType tokenizer = TweetClassifier.TokenizerType.WORD;
	private TweetClassifier.StemmerType stemmer = TweetClassifier.StemmerType.NONE;
	private int k = 10;
	private int features = 1000;
	private int ngrammin = -1;
	private boolean stopwords = true;
	private boolean evaluate = true;
	private String loadmodelfilename = null;
	private String savemodelfilename = null;

	
	// Output results
	
	private int totalInstances = 0;
	private int corclassified  = 0;
	private int incclassified = 0;
	private float kappa = 0;
	private float meanabserr = 0;
	private float rmserr = 0;
	private float relabserr = 0;
	private float rootrelsqerr = 0;
	private float coverage = 0;
	private float meanrelreg = 0;
	
	private double precision = 0;
	private double pctcorrect = 0;
	private double pctincorrect = 0;
	private double pctunclassified = 0;
	
	// confusion matrix
	private int tp = 0;
	private int tn = 0;
	private int fp = 0;
	private int fn = 0;
	
	public void setIn(String in) {
		this.in = in;
	}
	public String getIn() {
		return in;
	}
	public void setClassifier(TweetClassifier.ClassifierType classifier) {
		this.classifier = classifier;
	}
	public TweetClassifier.ClassifierType getClassifier() {
		return classifier;
	}
	public void setLoader(TweetClassifier.LoaderType loader) {
		this.loader = loader;
	}
	public TweetClassifier.LoaderType getLoader() {
		return loader;
	}
	public void setTokenizer(TweetClassifier.TokenizerType tokenizer) {
		this.tokenizer = tokenizer;
	}
	public TweetClassifier.TokenizerType getTokenizer() {
		return tokenizer;
	}
	public void setStemmer(TweetClassifier.StemmerType stemmer) {
		this.stemmer = stemmer;
	}
	public TweetClassifier.StemmerType getStemmer() {
		return stemmer;
	}
	public void setK(int k) {
		this.k = k;
	}
	public int getK() {
		return k;
	}
	public void setFeatures(int features) {
		this.features = features;
	}
	public int getFeatures() {
		return features;
	}
	public void setNgrammin(int ngrammin) {
		this.ngrammin = ngrammin;
	}
	public int getNgrammin() {
		return ngrammin;
	}
	public void setStopwords(boolean stopwords) {
		this.stopwords = stopwords;
	}
	public boolean isStopwords() {
		return stopwords;
	}
	public void setEvaluate(boolean evaluate) {
		this.evaluate = evaluate;
	}
	public boolean isEvaluate() {
		return evaluate;
	}
	public void setLoadModelFileName(String loadModelFileName) {
		if (!loadModelFileName.isEmpty())
			this.loadmodelfilename= loadModelFileName;
	}
	public String getLoadModelFileName() {
		return loadmodelfilename;
	}
	
	public void setSavemodelfilename(String savemodelfilename) {
		if (!savemodelfilename.isEmpty())
			this.savemodelfilename = savemodelfilename;
	}
	public String getSavemodelfilename() {
		return savemodelfilename;
	}
	
	public void setTotalInstances(int totalInstances) {
		this.totalInstances = totalInstances;
	}
	public int getTotalInstances() {
		return totalInstances;
	}
	public void setCorclassified(int corclassified) {
		this.corclassified = corclassified;
	}
	public int getCorclassified() {
		return corclassified;
	}
	public void setIncclassified(int incclassified) {
		this.incclassified = incclassified;
	}
	public int getIncclassified() {
		return incclassified;
	}
	public void setKappa(float kappa) {
		this.kappa = kappa;
	}
	public float getKappa() {
		return kappa;
	}
	public void setMeanabserr(float meanabserr) {
		this.meanabserr = meanabserr;
	}
	public float getMeanabserr() {
		return meanabserr;
	}
	public void setRmserr(float rmserr) {
		this.rmserr = rmserr;
	}
	public float getRmserr() {
		return rmserr;
	}
	public void setRelabserr(float relabserr) {
		this.relabserr = relabserr;
	}
	public float getRelabserr() {
		return relabserr;
	}
	public void setRootrelsqerr(float rootrelsqerr) {
		this.rootrelsqerr = rootrelsqerr;
	}
	public float getRootrelsqerr() {
		return rootrelsqerr;
	}
	public void setCoverage(float coverage) {
		this.coverage = coverage;
	}
	public float getCoverage() {
		return coverage;
	}
	public void setMeanrelreg(float meanrelreg) {
		this.meanrelreg = meanrelreg;
	}
	public float getMeanrelreg() {
		return meanrelreg;
	}
	public void setTp(int tp) {
		this.tp = tp;
	}
	public int getTp() {
		return tp;
	}
	public void setTn(int tn) {
		this.tn = tn;
	}
	public int getTn() {
		return tn;
	}
	public void setFp(int fp) {
		this.fp = fp;
	}
	public int getFp() {
		return fp;
	}
	public void setFn(int fn) {
		this.fn = fn;
	}
	public int getFn() {
		return fn;
	}
	
	public void readEval(Evaluation eval) {
		
		//// Output results
		totalInstances = (int)eval.numInstances();
		corclassified  = (int)eval.correct();
		incclassified = (int)eval.incorrect();
		kappa = (float)eval.kappa();
		meanabserr = (float)eval.meanAbsoluteError();
		rmserr = (float)eval.rootMeanSquaredError();
		try {
			relabserr = (float)eval.relativeAbsoluteError();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rootrelsqerr = (float)eval.rootRelativeSquaredError();
		coverage = (float)eval.coverageOfTestCasesByPredictedRegions();
		meanrelreg = (float)(eval.truePositiveRate(0)+eval.truePositiveRate(1))/2;
		
		// confusion matrix
		/*tp = (int)eval.numTruePositives(0);
		tn = (int)eval.numTrueNegatives(0);
		fp = (int)eval.numFalsePositives(0);
		fn = (int)eval.numFalseNegatives(0);*/
		tp = (int)eval.confusionMatrix()[0][0];
		tn = (int)eval.confusionMatrix()[0][1];
		fp = (int)eval.confusionMatrix()[1][0];
		fn = (int)eval.confusionMatrix()[1][1];
		
		
		precision = eval.precision(0);
		pctcorrect = eval.pctCorrect();
		pctincorrect = eval.pctIncorrect();
		pctunclassified = eval.pctUnclassified();
	}
	public void setPctcorrect(double pctcorrect) {
		this.pctcorrect = pctcorrect;
	}
	public double getPctcorrect() {
		return pctcorrect;
	}
	public void setPctincorrect(double pctincorrect) {
		this.pctincorrect = pctincorrect;
	}
	public double getPctincorrect() {
		return pctincorrect;
	}
	public void setPctunclassified(double pctunclassified) {
		this.pctunclassified = pctunclassified;
	}
	public double getPctunclassified() {
		return pctunclassified;
	}
	public void setPrecision(double precision) {
		this.precision = precision;
	}
	public double getPrecision() {
		return precision;
	}
	
	
	
	
}
