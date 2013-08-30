/**
 * 
 */
package org.neodatis.odb.core.oid.sequential;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer3.DataConverter;
import org.neodatis.odb.core.layers.layer3.DataConverterImpl;


/**
 * @author olivier
 *
 */
public class ClassOidImpl extends OIDImpl implements ClassOid {
	protected long classId; 
	protected String full;
	protected static DataConverter converter = new DataConverterImpl(false, null, NeoDatis.getConfig());
	
	/**
	 * @param type
	 * @param owner
	 * @param objectId
	 */
	public ClassOidImpl(long classId) {
		super(OIDTypes.TYPE_CLASS_OID);
		this.classId = classId;
	}
	
	public String toString() {
		return oidToString();
	}
	public String oidToString(){
		if(full==null){
			full = new StringBuilder(10).append(type).append(".").append(classId).toString();
		}
		
		return full;
	}
	public static ClassOid oidFromString(String s){
		String[] tokens = s.split("\\.");
		if(tokens.length!=2){
			throw new NeoDatisRuntimeException(NeoDatisError.INVALID_OID_REPRESENTATION.addParameter(s));
		}
		int type = Integer.parseInt(tokens[0]);
		
		if(type!=OIDTypes.TYPE_CLASS_OID){
			throw new NeoDatisRuntimeException(NeoDatisError.INVALID_OID_REPRESENTATION.addParameter(s));
		}
		long classId = Long.parseLong(tokens[1]);
		return new ClassOidImpl(classId);
	}

	public long getId() {
		return classId;
	}

	public int compareTo(Object obj) {
		if(obj==null || !(obj instanceof ClassOidImpl)){
			return -1000;
		}
		ClassOidImpl coi = (ClassOidImpl) obj;
		return (int) (classId-coi.classId) % Integer.MAX_VALUE;
	}
	
	public boolean equals(Object obj) {
		if(obj==null || !(obj instanceof ClassOidImpl)){
			return false;
		}
		ClassOidImpl coi = (ClassOidImpl) obj;
		
		return classId == coi.classId;
	}

	public int hashCode() {
		return (int)(classId ^ (classId >>> 32));
	}

	/* (non-Javadoc)
	 * @see org.neodatis.odb.OID#isNull()
	 */
	public boolean isNull() {
		return classId==-1;
	}

	public byte[] toByte() {
		byte[] b = new byte[8];
		converter.longToByteArray(classId, b,0,"coid");
		return b;
	}
}
