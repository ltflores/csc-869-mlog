package tools;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class TweetCounts {
	String dataDir = "data";

	public static void main(String args[]) throws Exception {
		TweetCounts tCnts = new TweetCounts(args);
		tCnts.run();
	}

	public TweetCounts(String args[]) {
		if (args.length>0)
			dataDir=args[0];
		else
			System.out.println("No folder selected, using '"+dataDir+"'\n");
	}

	private void run() throws Exception{
		File path = new File(dataDir);
		SvnFilter svnFilter = new SvnFilter();

		File files[]; 

		files = path.listFiles(svnFilter);

		System.out.println("folder,Rep,RepTweets,Dem,DemTweets,Tot,Tottweets");
		for (File file : files){
			if (isTweetFolder(file)){
				String folderName = file.toString().substring((path.toString()+File.separator).length());

				File[] parties = file.listFiles(svnFilter);
				
				int numRep = 0;
				int numDem = 0;
				int numDemTweets = 0;
				int numRepTweets = 0;
				
				for (File party : parties){
					String partyName = party.toString().substring((file.toString()+File.separator).length());
					File[] users = party.listFiles(svnFilter);
					
					if (partyName.contains("rep")){
						numRep = users.length;
						numRepTweets = getTweetCount(users);
					} else if (partyName.contains("dem")){
						numDem = users.length;
						numDemTweets = getTweetCount(users);
					}
				}
				System.out.println(folderName+","+numRep+","+numRepTweets+
						","+numDem+","+numDemTweets+","+(numRep+numDem)+","+(numDemTweets+numRepTweets));
			}

		}

	}
	
	private int getTweetCount(File[] users) throws Exception {
		int numTweets = 0;
		for (File user : users){
			numTweets += count(user);
		}
		return numTweets;
	}

	private boolean isTweetFolder(File folder){
		SvnFilter svnFilter = new SvnFilter();
		File[] subFolders = folder.listFiles(svnFilter);
		if (subFolders.length==2){
			if (subFolders[0].isDirectory()&&subFolders[1].isDirectory())
				return true;
		}
		return false;
	}

	/**
	 * optimized line count algorithm taken from somewhere on the vast internets...
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public int count(File filename) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			while ((readChars = is.read(c)) != -1) {
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n')
						++count;
				}
			}
			return count;
		} finally {
			is.close();
		}
	}

	/**
	 * Naive count implementation, for the sake of comparison
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public int count2(File filename) throws IOException {
		BufferedReader is = new BufferedReader(new FileReader(filename));
		int count=0;
		while (is.readLine()!=null){
			count++;
		}
		is.close();
		return count;
	}


}
