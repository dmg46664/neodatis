/**
 * 
 */
package org.neodatis.odb.core.oid.uuid;

import java.util.UUID;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.core.layers.layer3.DataConverter;
import org.neodatis.odb.core.layers.layer3.DataConverterImpl;
import org.neodatis.odb.core.oid.OIDTypes;

/**
 * @author olivier
 * 
 */
public class ClassOidImpl extends OIDImpl implements ClassOid {
	
	protected static DataConverter converter = new DataConverterImpl(false, null, NeoDatis.getConfig());
	
	protected UUID uuid; 
	/**
	 * @param type
	 * @param owner
	 * @param objectId
	 */
	public ClassOidImpl() {
		super();
	}
	public ClassOidImpl(UUID uuid) {
		super();
		this.uuid = uuid;
	}

	public int getType() {
		return OIDTypes.TYPE_CLASS_OID;
	}
	@Override
	public String toString() {
		return oidToString();
	}
	@Override
	public String oidToString() {
		if(isNull()){
			return "__null__";
		}
		return uuid.toString();
	}

	
	public byte[] toByte() {
		long l = 0;
		long m = 0;
		byte[] b = new byte[16];
		
		if(uuid!=null){
			l = uuid.getLeastSignificantBits();
			m = uuid.getMostSignificantBits();
		}
		converter.longToByteArray(l, b,0,"least");
		converter.longToByteArray(m, b,8,"most");
		return b;
	}
	public boolean isNull() {
		return uuid==null;
	}
	@Override
	public int hashCode() {
		if(uuid==null){
			return -1;
		}
		return uuid.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ClassOidImpl)){
			return false;
		}
		if(uuid==null || obj==null){
			return false;
		}
		ClassOidImpl uuid2 = (ClassOidImpl) obj;
		return uuid.equals(uuid2.uuid);
	}
	
	public static ClassOid classOidfromString(String s) {
		UUID classUuid = UUID.fromString(s);
		return new ClassOidImpl(classUuid);
	}

}
