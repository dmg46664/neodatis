package org.neodatis.odb.core.layers.layer4;

import org.neodatis.odb.ClassOid;

public interface ClassOidIterator extends Iterable<ClassOid>{
	 
	boolean hasNext();
	ClassOid next();
	void reset();
}
