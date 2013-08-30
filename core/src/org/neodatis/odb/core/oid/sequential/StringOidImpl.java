/**
 * 
 */
package org.neodatis.odb.core.oid.sequential;

import org.neodatis.odb.OID;
import org.neodatis.odb.core.oid.StringOid;


/**
 * @author olivier
 * 
 */
public class StringOidImpl extends OIDImpl implements StringOid {
	protected String s;

	public StringOidImpl(String s) {
		super(OIDTypes.TYPE_STRING_OID);
		this.s = s;
	}
	
	public StringOidImpl(String prefix, OID oid) {
		super(OIDTypes.TYPE_STRING_OID);
		this.s = new StringBuffer(prefix).append("-").append(oid.oidToString()).toString();
	}

	public String oidToString() {
		return new StringBuilder(15).append(type).append(".").append(s).toString();
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
		// remove 10.
		String realString = s.substring(3);
		return new StringOidImpl(realString);
	}

	public boolean isNull() {
		return s==null || s.length()==0;
	}

	public byte[] toByte() {
		return s.getBytes();
	}

}
