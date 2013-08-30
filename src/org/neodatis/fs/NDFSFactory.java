/**
 * 
 */
package org.neodatis.fs;

import org.neodatis.fs.io.NDFSImpl;

/**
 * @author olivier
 *
 */
public class NDFSFactory {
	public static NDFS open(NdfsConfig config){
		return new NDFSImpl(config);
	}

	public static NdfsConfig getConfig(String directory, int blockSize) {
		NdfsConfig config = new NdfsConfig(directory, blockSize);
		return config;
	}
	public static NdfsConfig getConfig(String directory) {
		NdfsConfig config = new NdfsConfig(directory);
		return config;
	}

}
