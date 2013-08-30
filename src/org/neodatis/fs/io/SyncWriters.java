/**
 * 
 */
package org.neodatis.fs.io;

import java.util.HashMap;
import java.util.Map;

import org.neodatis.fs.NDFS;
import org.neodatis.fs.NdfsFile;

/**
 * @author olivier
 *
 */
public class SyncWriters {
	
	protected Map<Long, SyncBlockWriter> writers;
	protected NDFS ndfs;
	
	public SyncWriters(NDFS ndfs){
		this.ndfs = ndfs;
		this.writers = new HashMap<Long, SyncBlockWriter>();
	}
	
	public SyncBlockWriter getSyncWriter(NdfsFile file){
		SyncBlockWriter writer = writers.get(file.getId());
		
		if(writer==null){
			synchronized (writers) {
				writer = new SyncBlockWriter(file);
				writers.put(file.getId(), writer);
			}
		}
		return writer;
	}
}
