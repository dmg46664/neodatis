/**
 * 
 */
package org.neodatis.odb.core.oid.uuid;

import java.util.UUID;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer3.DataConverter;
import org.neodatis.odb.core.layers.layer3.DataConverterImpl;
import org.neodatis.odb.core.oid.OIDTypes;

/**
 * @author olivier
 * 
 */
public class ObjectOidImpl extends OIDImpl implements ObjectOid {

	private static final String SEP = "@";
	protected ClassOid classOid;
	public static int i=0;
	protected static DataConverter converter = new DataConverterImpl(false, null, NeoDatis.getConfig());
	
	protected UUID uuid;

	/**
	 * @param type
	 * @param owner
	 * @param objectId
	 */
	public ObjectOidImpl() {
		super();
	}

	public ObjectOidImpl(UUID uuid, ClassOid classOid) {
		super();
		this.classOid = classOid;
		this.uuid = uuid;
	}

	public String toString() {
		return oidToString();
	}

	public int compareTo(Object obj) {
		if (obj == null || !(obj instanceof ObjectOidImpl) || uuid==null) {
			return -1000;
		}
		ObjectOidImpl ooi = (ObjectOidImpl) obj;
		// TODO check if this correct : we don t use class oid here
		return uuid.compareTo(ooi.uuid);
	}

	public int getType() {
		return OIDTypes.TYPE_OBJECT_OID;
	}

	public ClassOid getClassOid() {
		return classOid;
	}

	public void setClassOid(ClassOid classOid) {
		this.classOid = classOid;
	}

	@Override
	public String oidToString() {
		if(uuid==null){
			return "null-oid";
		}
		return new StringBuilder(uuid.toString()).append(SEP).append(classOid.oidToString()).toString(); 
	}

	public byte[] toByte() {
		byte[] b = new byte[32];
		ClassOidImpl coid = (ClassOidImpl) classOid;
		
		long lc = 0;
		long mc = 0;
		long lo = 0;
		long mo = 0;
		
		if(uuid!=null){
			lc = coid.uuid.getLeastSignificantBits();
			mc = coid.uuid.getMostSignificantBits();

			lo = uuid.getLeastSignificantBits();
			mo = uuid.getMostSignificantBits();
		}
		
		converter.longToByteArray(lc, b,0,"least class");
		converter.longToByteArray(mc, b,8,"most class");
		converter.longToByteArray(lo, b,16,"least object");
		converter.longToByteArray(mo, b,24,"most object");
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
		if(!(obj instanceof ObjectOidImpl)){
			return false;
		}
		if(uuid==null || obj==null){
			return false;
		}
		ObjectOidImpl oid2 = (ObjectOidImpl) obj;
		return uuid.equals(oid2.uuid);
	}

	public static ObjectOid objectOidfromString(String s) {
		String[] tokens = s.split(SEP);
		UUID objectUuid = UUID.fromString(tokens[0]);
		UUID classUuid = UUID.fromString(tokens[1]);
		return new ObjectOidImpl(objectUuid, new ClassOidImpl(classUuid));
	}

}
