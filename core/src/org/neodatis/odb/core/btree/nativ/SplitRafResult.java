/**
 * 
 */
package org.neodatis.odb.core.btree.nativ;

import java.io.RandomAccessFile;

/**
 * @author olivier
 *
 */
public class SplitRafResult {
	public long adjustedPosition;
	public RandomAccessFile raf;
	
	public SplitRafResult(long adjustedPosition, RandomAccessFile raf) {
		super();
		this.adjustedPosition = adjustedPosition;
		this.raf = raf;
	}
	

}
