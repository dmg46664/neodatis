package org.neodatis.odb.core;

import org.neodatis.odb.OID;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.tool.wrappers.list.IOdbList;

public interface DatabaseEngine {
	
	// storing from layer 1 to layer 4
	NonNativeObjectInfo layer1ToLayer2(Object object);
	IOdbList<OidAndBytes> layer2ToLayer3(NonNativeObjectInfo nnoi);
	void layer3ToLayer4(IOdbList<OidAndBytes> oidsAndBytes);
	
	// retrieving from layer 4 to layer 1
	IOdbList<OidAndBytes> layer4ToLayer3(OID oid);
	NonNativeObjectInfo layer3ToLayer2(IOdbList<OidAndBytes> oidAndBytes);
	Object layer2ToLayer1(NonNativeObjectInfo nnoi);
	
}
