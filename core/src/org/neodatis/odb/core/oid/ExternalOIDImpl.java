package org.neodatis.odb.core.oid;

import org.neodatis.odb.DatabaseId;
import org.neodatis.odb.ExternalOID;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.oid.uuid.OIDImpl;
import org.neodatis.tool.wrappers.OdbString;

public class ExternalOIDImpl extends OIDImpl implements ExternalOID{
	private DatabaseId databaseId;
	protected OID oid;
	public ExternalOIDImpl(OID oid, DatabaseId databaseId) {
		super();
		this.oid = oid;
		this.databaseId = databaseId;
	}
	public DatabaseId getDatabaseId() {
		return databaseId;
	}
	
	public String oidToString() {
		return oid.oidToString();
		//StringBuffer buffer = new StringBuffer(oid.oidToString());.append("@").append(databaseId);
		//return buffer.toString();
	}

	public static ExternalOIDImpl oidFromString(String oidString){
		String [] tokens = OdbString.split(oidString,"@");
		if(tokens.length!=2){
			throw new NeoDatisRuntimeException(NeoDatisError.INVALID_OID_REPRESENTATION.addParameter(oidString));
		}
		String databaseId = tokens[1];
		String oidRepresention = tokens[0];
		return null;//return new ExternalOIDImpl(OIDImpl.oidFromString(oidRepresention), DatabaseIdImpl.fromString(databaseId));
	}
	
	
	public boolean isNull() {
		return oid==null || oid.isNull();
	}
	public byte[] toByte() {
		return oid.toByte();
	}
	
}
