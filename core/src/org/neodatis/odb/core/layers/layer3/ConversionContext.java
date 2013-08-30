package org.neodatis.odb.core.layers.layer3;

import java.util.HashMap;
import java.util.Map;

import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;

public class ConversionContext {
	public Map<OID, NonNativeObjectInfo> alreadyConvertedOids;
	protected IOdbList<OidAndBytes> oidAndBytes;
	public Map<OID, OidAndBytes> oabByObjectId;
	public Map<OID, OID> oidsToReplace;
	public ObjectOid lastOid;
	public int recursionLevel;
	public boolean isMainObject;
	public OidAndBytes mainOab;
	/** To indicate till what recursive depth the conversion must take place*/
	public int depth;

	public ConversionContext() {
		alreadyConvertedOids = new HashMap<OID, NonNativeObjectInfo>();
		oidAndBytes = new OdbArrayList<OidAndBytes>();
		recursionLevel = 0;
		isMainObject = true;
		depth = 0;
	}

	public boolean isMainObject() {
		return isMainObject;
	}

	public void setIsMainObject(boolean isMainObject) {
		this.isMainObject = isMainObject;
	}

	/**
	 * @param oabs
	 * @param oidsToReplace
	 */
	public ConversionContext(IOdbList<OidAndBytes> oabs, Map<OID, OID> oidsToReplace) {
		alreadyConvertedOids = new HashMap<OID, NonNativeObjectInfo>();
		this.oidAndBytes = oabs;
		this.oidsToReplace = oidsToReplace;

		// to enable fast access build a map to access via ObjectId
		oabByObjectId = new HashMap<OID, OidAndBytes>();
		for (OidAndBytes oab : oabs) {
			oabByObjectId.put(oab.oid, oab);
		}
	}

	public OidAndBytes getOabWithOid(OID oid) {
		if (oabByObjectId == null) {
			return null;
		}
		return oabByObjectId.get(oid);
	}

	public void addOidAndBytes(OID oid, Bytes bytes, NonNativeObjectInfo objectInfo, boolean isMain) {
		OidAndBytes oab = new OidAndBytes(oid, bytes, objectInfo);
		if (!isMain) {
			oidAndBytes.add(oab);
		} else {
			mainOab = oab;
		}
	}

	/**
	 * Be sure the main oab is at the first position
	 * 
	 */
	protected void order() {
		oidAndBytes.add(0, mainOab);
		mainOab = null;
	}

	public IOdbList<OidAndBytes> getOidAndBytes() {
		if (mainOab != null) {
			order();
		}
		return oidAndBytes;
	}

	/**
	 * @param existingOid
	 */
	public void setLastOid(ObjectOid existingOid) {
		this.lastOid = existingOid;
	}

	/**
	 * @return
	 */
	public ObjectOid getLastOid() {
		return lastOid;
	}

	/** @depracated
	 * 
	 * @param oid
	 * @return
	 */
	public OID replaceOid(OID oid) {
		if (oidsToReplace == null) {
			return oid;
		}
		OID newOid = oidsToReplace.get(oid);
		if (newOid == null) {
			return oid;
		}
		return newOid;
	}

	/**
	 * @param depth
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getDepth() {
		return depth;
	}
	
	
}
