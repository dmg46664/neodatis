/**
 * 
 */
package org.neodatis.odb.core.btree.nativ;

import org.neodatis.fs.NdfsFile;

/**
 * @author olivier
 *
 */
public class SplitRafResult2 {
	public long adjustedPosition;
	public NdfsFile file;
	
	public SplitRafResult2(long adjustedPosition, NdfsFile file) {
		super();
		this.adjustedPosition = adjustedPosition;
		this.file = file;
	}
	

}
