/**
 * 
 */
package org.neodatis.odb.core.oid.uuid;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.DatabaseId;
import org.neodatis.odb.ExternalOID;
import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer3.DataConverter;
import org.neodatis.odb.core.layers.layer3.DataConverterImpl;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.oid.ExternalOIDImpl;
import org.neodatis.odb.core.oid.OidGeneratorAdapter;

/**
 * @author olivier
 * 
 */
public class UniqueOidGeneratorImpl extends OidGeneratorAdapter {
	public static final ClassOid NULL_CLASS_OID = new ClassOidImpl(null);
	public static final ObjectOid NULL_OBJECT_OID = new ObjectOidImpl(null,null);
	public static final String SEP = "@";

	protected static DataConverter converter = new DataConverterImpl(false, null, NeoDatis.getConfig());
	
	protected Map<UUID, ClassOid> coids;
	
	public UniqueOidGeneratorImpl() {
		coids = new HashMap<UUID, ClassOid>();
	}

	public synchronized ClassOid createClassOid() {
		UUID uuid = UUID.randomUUID();
		return new ClassOidImpl(uuid);
	}

	public synchronized ObjectOid createObjectOid(ClassOid classOid) {
		UUID uuid = UUID.randomUUID();
		return new ObjectOidImpl(uuid,classOid);
	}

	public ClassOid buildClassOID(byte[] bb) {
		if(bb.length==0){
			return NULL_CLASS_OID;
		}
		
		long least = converter.byteArrayToLong(bb, 0, "least");
		long most = converter.byteArrayToLong(bb, 8, "most");
		
		if(least==0 && most==0){
			return NULL_CLASS_OID;
		}
		UUID uuid = new UUID(most,least);
		
		ClassOid coid = coids.get(uuid);
		if(coid!=null){
			return coid;
		}
		coid = new ClassOidImpl(uuid);
		coids.put(uuid, coid);
		
		return coid;
	}

	public ObjectOid buildObjectOID(byte[] bb) {
		if(bb.length==0){
			return NULL_OBJECT_OID;
		}
		
		long leastC = converter.byteArrayToLong(bb, 0, "least");
		long mostC = converter.byteArrayToLong(bb, 8, "most");
		long leastO = converter.byteArrayToLong(bb, 16, "least");
		long mostO = converter.byteArrayToLong(bb, 24, "most");
		
		if(leastO==0 && mostO==0){
			return NULL_OBJECT_OID;
		}
		UUID uuidClass = new UUID(mostC , leastC);
		UUID uuidObject = new UUID(mostO,leastO);
		
		ClassOid coid = coids.get(uuidClass);
		if(coid==null){
			coid = new ClassOidImpl(uuidClass);
			coids.put(uuidClass, coid);
		}
		
		return new ObjectOidImpl(uuidObject, coid);
		
	}
	
	public OID buildStringOid(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	public ExternalOID toExternalOid(ObjectOid oid, DatabaseId databaseId) {
		return new ExternalOIDImpl(oid, databaseId);
	}

	public ClassOid getNullClassOid() {
		return NULL_CLASS_OID;
	}

	public ObjectOid getNullObjectOid() {
		return NULL_OBJECT_OID;
	}

	public void commit() {
		// This oid generator does not use cache, so  there is nothing to commit
	}
	
	public ObjectOid objectOidFromString(String s) {
		return ObjectOidImpl.objectOidfromString(s);
	}

	public ClassOid classOidFromString(String s) {
		return ClassOidImpl.classOidfromString(s);
	}

	public String getSimpleName() {
		return "unique";
	}

}
