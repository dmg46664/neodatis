/**
 * 
 */
package org.neodatis.odb.core.oid.sequential;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer3.DataConverter;
import org.neodatis.odb.core.layers.layer3.DataConverterImpl;

/**
 * @author olivier
 * 
 */
public class ObjectOidImpl extends OIDImpl implements ObjectOid {

	protected static DataConverter converter = new DataConverterImpl(false, null, NeoDatis.getConfig());
	
	protected ClassOid classOid;
	protected long objectId;
	protected String text;

	/**
	 * @param type
	 * @param owner
	 * @param objectId
	 */
	public ObjectOidImpl(ClassOid classOid, long objectId) {
		super(OIDTypes.TYPE_OBJECT_OID);
		this.classOid = classOid;
		this.objectId = objectId;
	}

	public String toString() {
		return oidToString();
	}

	public static ObjectOid oidFromString(String s) {
		
		String[] tokens = s.split("\\.");
		if (tokens.length != 3) {
			throw new NeoDatisRuntimeException(NeoDatisError.INVALID_OID_REPRESENTATION.addParameter(s));
		}
		int type = Integer.parseInt(tokens[0]);
		if(type!=OIDTypes.TYPE_OBJECT_OID){
			throw new NeoDatisRuntimeException(NeoDatisError.INVALID_OID_REPRESENTATION.addParameter(s));
		}
		
		long classId = Long.parseLong(tokens[1]);
		long objectId = Long.parseLong(tokens[2]);
		return new ObjectOidImpl(new ClassOidImpl(classId), objectId);
	}

	public String oidToString() {
		if(text!=null){
			return text;
		}
		text = new StringBuilder(15).append(type).append(".").append( ((ClassOidImpl) classOid).classId).append(".").append(objectId).toString();
		return text;
	}

	public byte[] oidToByte(){
		byte[] bytes = new byte[12];
		return bytes;
	}
	public int compareTo(Object obj) {
		if(obj==null || !(obj instanceof ObjectOidImpl) ){
			return -1000;
		}
		ObjectOidImpl ooi = (ObjectOidImpl) obj;
		int difClassId = classOid.compareTo(ooi.classOid);
		if(difClassId!=0){
			return difClassId;
		}

		int difObjectId = (int) (objectId-ooi.objectId)%Integer.MAX_VALUE;
		return difObjectId;
		
	}
	
	public boolean equals(Object obj) {
		return this.compareTo(obj)==0;
	}

	public ClassOid getClassOid() {
		return classOid;
	}

	public long getObjectId() {
		return objectId;
	}
	public int hashCode() {
		return (int)(objectId ^ (objectId >>> 32));
	}

	/* (non-Javadoc)
	 * @see org.neodatis.odb.OID#isNull()
	 */
	public boolean isNull() {
		return objectId==0;
	}

	public void setClassOid(ClassOid classOid) {
		// TODO Auto-generated method stub
		
	}

	public byte[] toByte() {
		byte[] b = new byte[8+8];
		ClassOidImpl coid = (ClassOidImpl) getClassOid();
		
		converter.longToByteArray(objectId, b,0,"oid");
		converter.longToByteArray(coid.classId, b,8,"coid");
		return b;
	}

	


}
