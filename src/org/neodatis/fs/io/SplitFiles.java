/**
 * 
 */
package org.neodatis.fs.io;

import java.util.ArrayList;
import java.util.List;

/**
 * @author olivier
 *
 */
public class SplitFiles {
	List<OneSplitFileResult> splits;
	
	public SplitFiles(){
		splits = new ArrayList<OneSplitFileResult>();
	}
	
	public void addSplit(OneSplitFileResult r){
		splits.add(r);
	}
	public List<OneSplitFileResult> getSplits(){
		return splits;
	}

}
