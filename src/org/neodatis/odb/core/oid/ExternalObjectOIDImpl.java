package org.neodatis.odb.core.oid;

import org.neodatis.odb.DatabaseId;
import org.neodatis.odb.ExternalObjectOid;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.oid.uuid.ObjectOidImpl;

public class ExternalObjectOIDImpl extends ObjectOidImpl implements ExternalObjectOid{
	protected static String EXT_SEP = "$";
	protected ObjectOid oid;
	protected DatabaseId databaseId;
	public ExternalObjectOIDImpl(ObjectOid oid, DatabaseId databaseId) {
		super();
		this.oid = oid;
		this.databaseId = databaseId;
	}
	
	public DatabaseId getDatabaseId() {
		// TODO Auto-generated method stub
		return databaseId;
	}
	
	@Override
	public String oidToString() {
		StringBuilder b = new StringBuilder(databaseId.toString()).append(EXT_SEP).append(oid.oidToString());
		return b.toString(); 
	}
	
}
