package classifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.Instances;

import weka.core.converters.TextDirectoryLoader;

/**
 * Replaces getStructure() method of TextDirectoryLoader to ignore svn folders...
 * that is all
 * @author ltflores
 *
 */
public class CustmTextDirectoryLoader extends TextDirectoryLoader {

	/**
	 * Eclipse wanted this...so I gave in...
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Determines and returns (if possible) the structure (internally the 
	 * header) of the data set as an empty set of instances.
	 * 
	 * This is a modified version of the TextDirectoryLoader getStructure
	 * method which ignores .svn folders.
	 *
	 * @return 			the structure of the data set as an empty 
	 * 				set of Instances
	 * @throws IOException 	if an error occurs
	 */
	@Override
	public Instances getStructure() throws IOException {
		if (getDirectory() == null) {
			throw new IOException("No directory/source has been specified");
		}

		// determine class labels, i.e., sub-dirs
		if (m_structure == null) {
			String directoryPath = getDirectory().getAbsolutePath();
			ArrayList<Attribute> atts = new ArrayList<Attribute>();
			ArrayList<String> classes = new ArrayList<String>();

			File dir = new File(directoryPath);
			String[] subdirs = dir.list();

			for (int i = 0; i < subdirs.length; i++) {
				File subdir = new File(directoryPath + File.separator + subdirs[i]);
				//Ignore svn folders
				if (subdir.isDirectory()&&!(subdir.getName().contains(".svn")))
					classes.add(subdirs[i]);
			}

			atts.add(new Attribute("text", (ArrayList<String>) null));
			if (m_OutputFilename)
				atts.add(new Attribute("filename", (ArrayList<String>) null));
			// make sure that the name of the class attribute is unlikely to 
			// clash with any attribute created via the StringToWordVector filter
			atts.add(new Attribute("@@class@@", classes));

			String relName = directoryPath.replaceAll("/", "_");
			relName = relName.replaceAll("\\\\", "_").replaceAll(":", "_");
			m_structure = new Instances(relName, atts, 0);    
			m_structure.setClassIndex(m_structure.numAttributes() - 1);
		}

		return m_structure;
	}
}
