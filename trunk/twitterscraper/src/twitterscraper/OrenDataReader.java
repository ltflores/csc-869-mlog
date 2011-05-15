package twitterscraper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class OrenDataReader {
	
	public static void main(String[] args){
		
		File file=null;
		FileReader reader = null;
		BufferedReader s = null;
		File fileO=null;
		FileWriter writer = null;
		BufferedWriter w = null;
		
		try {
			file = new File("oren-list.txt");
			fileO = new File("oren-democrats.csv");
		} catch (NullPointerException e) {
			System.out.println("DataReader error: "+e);
		}
		
		try {
			reader = new FileReader(file);
			writer = new FileWriter(fileO);
		} catch (Exception e) {
			System.out.println("DataReader error: "+e);
		}
		
		//Create a buffered reader
		s = new BufferedReader(reader);
		w = new BufferedWriter(writer);
		boolean isFirstLine = true;
		
		//Read in attributes and classes
		try {
			String in;
			while ((in = s.readLine()) != null){
				if (in.contains(" D")&&in.contains("@")){
					String user = in.substring(0, in.length()-3);
					System.out.println(user);
					w.write(user+"\n");
					w.flush();
				}
			}
			
		} catch (IOException e) {
			System.out.println("NamesLoader error: "+e);
		}
		
	}

}
