package org.neodatis.odb.core.layers.layer3;

import org.neodatis.odb.OID;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;

public class OidAndBytes {
	
	public static final int TYPE_OBJECT = 1;
	public static final int TYPE_CLASS_INFO = 2;
	public static final int TYPE_INDEX = 3;
	
	
	public int type;
	
	public OID oid;
	public Bytes bytes;
	public NonNativeObjectInfo nnoi;
	public int realObjectSize;
	public OidAndBytes(OID oid, Bytes bytes, NonNativeObjectInfo objectInfo) {
		super();
		this.type = TYPE_OBJECT;
		this.oid = oid;
		this.bytes = bytes;
		this.realObjectSize = bytes.getRealSize();
		this.nnoi = objectInfo;
	}
	/**
	 * @param oid2
	 * @param bytes2
	 */
	public OidAndBytes(OID oid, Bytes bytes) {
		this.oid = oid;
		this.bytes = bytes;
		this.realObjectSize = bytes.getRealSize();
	}
	public String toString() {
		return String.format("Oid=%s - real size=%d", oid,realObjectSize);
	}
	/**
	 * @return
	 */
	public boolean isObject() {
		return type==TYPE_OBJECT;
	}
}
 