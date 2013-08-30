/**
 * 
 */
package org.neodatis.fs.memory;

import java.util.HashMap;
import java.util.Map;

/**Global memory area
 * @author olivier
 *
 */
public class GMA {
	protected Map<FileAndBlockKey, byte[]> byteArrayByBlockId;
	public GMA(){
		this.byteArrayByBlockId = new HashMap<FileAndBlockKey, byte[]>();
	}

	public void merge(TMA tma){
		byteArrayByBlockId.putAll(tma.getByteArrayByBlockId());
	}

	public Map<FileAndBlockKey, byte[]> getByteArrayByBlockId() {
		return byteArrayByBlockId;
	}

}
