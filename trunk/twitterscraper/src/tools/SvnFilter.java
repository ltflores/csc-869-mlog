package tools;

import java.io.FilenameFilter;
import java.io.File;

public class SvnFilter implements FilenameFilter {
	
	public SvnFilter(){}
	
	public boolean accept(File dir, String name){
		if (name.contains(".svn"))
			return false;
		else
				return true;
	}

}
