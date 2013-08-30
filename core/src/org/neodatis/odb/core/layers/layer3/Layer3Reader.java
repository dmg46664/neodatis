/**
 * 
 */
package org.neodatis.odb.core.layers.layer3;

import java.util.HashSet;
import java.util.Map;

import org.neodatis.odb.OID;
import org.neodatis.odb.core.layers.layer2.meta.AttributeValuesMap;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.tool.wrappers.list.IOdbList;

/**
 * @author olivier
 *
 */
public interface Layer3Reader {
	NonNativeObjectInfo metaFromBytes(OidAndBytes oidAndBytes, boolean full, int depth);
	ClassInfo classInfoFromBytes(OidAndBytes oidAndBytes, boolean full);

	AttributeValuesMap valuesFromBytes(OidAndBytes oab, HashSet<String> attributeNames, int depth);
	public NonNativeObjectInfo metaFromBytes(IOdbList<OidAndBytes> oabs, boolean full, Map<OID, OID> oidsToReplace, int depth);
}
