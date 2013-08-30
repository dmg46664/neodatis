/**
 * 
 */
package org.neodatis.odb.core.layers.layer3;

import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.session.DatabaseInfo;
import org.neodatis.tool.wrappers.list.IOdbList;

/**
 * @author olivier
 *
 */
public interface Layer3Writer {
	IOdbList<OidAndBytes>  metaToBytes(NonNativeObjectInfo nnoi);
	IOdbList<OidAndBytes>  classInfoToBytes(ClassInfo ci);
	void writeDatabaseHeader(DatabaseInfo di);
}
