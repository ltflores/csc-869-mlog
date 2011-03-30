package twitterscraper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import au.com.bytecode.opencsv.CSVReader;

public class Twitterscraper {

	public static void main(String[] args) throws IOException, TwitterException {
		if(args.length!=2) {
			System.out.println("Usage: java twitterscraper.Twitterscraper republicans.csv democrats.csv");
			System.exit(-1);
		}
		
	    CSVReader reader = new CSVReader(new FileReader(args[0]));
	    List<String[]> republicans = reader.readAll();
	    
	    reader = new CSVReader(new FileReader(args[1]));
	    List<String[]> democrats = reader.readAll();

	    File repOutput = new File("republicans");
	    repOutput.mkdir();
	    
	    File demOutput = new File("democrats");
	    demOutput.mkdir();
	    
	    outputTweetsOfUser(republicans, repOutput);
	    outputTweetsOfUser(democrats, demOutput);
	}
	
	private static void outputTweetsOfUser(List<String[]> lines, File outputDir) throws IOException {
	    for(String[] line : lines) {
	    	Twitter twitter = (new TwitterFactory()).getInstance();
	    	
	    	Paging paging = new Paging(1, 100);
	    	List<Status> statuses;
			try {
				statuses = twitter.getUserTimeline(line[1].substring(1), paging);
			} catch (TwitterException e) {
				e.printStackTrace();
				continue;
			}
	    	
	    	String fileName = line[0].replace(" ", "");
	    	BufferedWriter output = new BufferedWriter(new FileWriter(outputDir.getAbsolutePath() + File.separator + fileName));
	    	
	    	for(Status each : statuses) {
	    		String text = each.getText();
				output.write(text+"\n");
				System.out.println(text);
	    	}
	    	
	    	output.flush();
	    	output.close();
	    }
	}
	
}
