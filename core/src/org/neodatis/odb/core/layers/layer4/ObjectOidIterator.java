package org.neodatis.odb.core.layers.layer4;

import org.neodatis.odb.ObjectOid;

public interface ObjectOidIterator {
	 
	void startAtTheEnd();
	void startAtTheBeginning();
	boolean hasNext();
	ObjectOid next();
	void reset();
	
	public enum Way{
		INCREASING,DECREASING;
	}
}
