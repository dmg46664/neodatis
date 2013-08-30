/**
 * 
 */
package org.neodatis.odb.core.oid;

import org.neodatis.odb.OID;
import org.neodatis.odb.core.oid.uuid.OIDImpl;


/**
 * @author olivier
 * 
 */
public class StringOidImpl extends OIDImpl implements StringOid{
	protected String s;

	public StringOidImpl(String s) {
		super();
		this.s = s;
	}
	
	public String oidToString() {
		return s;
	}

	public int compareTo(Object o) {
		if (o == null || !(o instanceof StringOidImpl)) {
			return -1000;
		}
		StringOidImpl so = (StringOidImpl) o;
		return s.compareTo(so.s);
	}

	public boolean equals(Object obj) {
		return this.compareTo(obj)==0;
	}

	public int hashCode() {
		return s.hashCode();
	}
	
	public static OID oidFromString(String s) {
		return new StringOidImpl(s);
	}

	public boolean isNull() {
		return s==null || s.length()==0;
	}
	public static StringOidImpl fromString(String s){
		return new StringOidImpl(s);
	}

	public byte[] toByte() {
		// TODO Auto-generated method stub
		return null;
	}

}
