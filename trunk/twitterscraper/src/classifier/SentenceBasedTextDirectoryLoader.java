/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package classifier;

/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 * TextDirectoryLoader.java
 * Copyright (C) 2006 University of Waikato, Hamilton, New Zealand
 *
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.core.DenseInstance;
import weka.core.Instances;

/**
 * <!-- globalinfo-start --> Loads all text files in a directory and uses the
 * subdirectory names as class labels. The content of the text files will be
 * stored in a String attribute, the filename can be stored as well.
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * <!-- options-start --> Valid options are:
 * <p/>
 * 
 * <pre>
 * -D
 *  Enables debug output.
 *  (default: off)
 * </pre>
 * 
 * <pre>
 * -F
 *  Stores the filename in an additional attribute.
 *  (default: off)
 * </pre>
 * 
 * <pre>
 * -dir &lt;directory&gt;
 *  The directory to work on.
 *  (default: current directory)
 * </pre>
 * 
 * <!-- options-end -->
 * 
 * Based on code from the TextDirectoryToArff tool:
 * <ul>
 * <li><a href=
 * "https://list.scms.waikato.ac.nz/mailman/htdig/wekalist/2002-October/000685.html"
 * target="_blank">Original tool</a></li>
 * <li><a href=
 * "https://list.scms.waikato.ac.nz/mailman/htdig/wekalist/2004-January/002160.html"
 * target="_blank">Current version</a></li>
 * <li><a href="http://weka.wikispaces.com/ARFF+files+from+Text+Collections"
 * target="_blank">Wiki article</a></li>
 * </ul>
 * 
 * @author Ashraf M. Kibriya (amk14 at cs.waikato.ac.nz)
 * @author Richard Kirkby (rkirkby at cs.waikato.ac.nz)
 * @author fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 6766 $
 * @see Loader
 */
public class SentenceBasedTextDirectoryLoader extends CustomTextDirectoryLoader {

	private final Logger logger = Logger.getLogger(SentenceBasedTextDirectoryLoader.class.getName());
	
	/**
	 * Return the full data set. If the structure hasn't yet been determined by
	 * a call to getStructure then method should do so before processing the
	 * rest of the data set.
	 * 
	 * @return the structure of the data set as an empty set of Instances
	 * @throws IOException
	 *             if there is no source or parsing fails
	 */
	@Override
	public Instances getDataSet() throws IOException {
		if (getDirectory() == null)
			throw new IOException("No directory/source has been specified");

		String directoryPath = getDirectory().getAbsolutePath();
		ArrayList<String> classes = new ArrayList<String>();
		ArrayList<String> filenames = new ArrayList<String>();
		Enumeration enm = getStructure().classAttribute().enumerateValues();
		while (enm.hasMoreElements())
			classes.add((String) enm.nextElement());

		Instances data = getStructure();
		int fileCount = 0;
		// each class is actually the filename - this is preserved around weka,
		// so its useful for tracking associations later and using as an "index"
		//

		for (int k = 0; k < classes.size(); k++) {
			String subdirPath = (String) classes.get(k);
			File subdir = new File(directoryPath + File.separator + subdirPath);
			String[] files = subdir.list();
			for (int j = 0; j < files.length; j++) {

				try {
					fileCount++;
					if (getDebug())
						System.err.println("processing " + fileCount + " : "
								+ files[j]);

					File txt = new File(directoryPath + File.separator
							+ subdirPath + File.separator + files[j]);
					filenames.add(files[j]);
					BufferedInputStream is;
					is = new BufferedInputStream(new FileInputStream(txt));
					StringBuffer txtStr = new StringBuffer();
					int c;
					while ((c = is.read()) != -1) {
						txtStr.append((char) c);
					}

					// Here is my extension to Text Directory Loader.
					String regexSentenceSplit = "(\\n)";
					String rawtext = txtStr.toString();
					rawtext = rawtext.toLowerCase();
					rawtext.trim();


					// split the sentences
					String[] sentences = rawtext.split(regexSentenceSplit);
					for (String sentence : sentences) {
						double[] newInst = null;
						if (m_OutputFilename)
							newInst = new double[3];
						else
							newInst = new double[2];

						newInst[0] = (double) data.attribute(0).addStringValue(
								sentence + "\n");
						if (m_OutputFilename)
							newInst[1] = (double) data.attribute(1)
									.addStringValue(
											subdirPath + File.separator
													+ files[j]);
						newInst[data.classIndex()] = (double) k;
						data.add(new DenseInstance(1.0, newInst));
						// }
					}

					writeFilenames(directoryPath, filenames);

				} catch (Exception e) {
					System.err.println("failed to convert file: "
							+ directoryPath + File.separator + files[j]);
				}
			}
		}

		// this.m_structure.setClassIndex(-1);
		return data;
	}

	private void writeFilenames(String directoryPath,
			ArrayList<String> filenames) {
		FileWriter outFile = null;
		try {
			outFile = new FileWriter(directoryPath + File.separator
					+ "familyfiles.txt");
			PrintWriter out = new PrintWriter(outFile);
			for (String file : filenames)
				out.println(file);
			out.close();
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		} finally {
			try {
				outFile.close();
			} catch (IOException ex) {
				logger.log(Level.SEVERE, null, ex);
			}
		}
	}

}
