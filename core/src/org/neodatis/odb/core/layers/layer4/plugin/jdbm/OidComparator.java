/**
 * 
 */
package org.neodatis.odb.core.layers.layer4.plugin.jdbm;

import java.io.Serializable;
import java.util.Comparator;

import org.neodatis.odb.OID;

/**
 * @author olivier
 * 
 */
public class OidComparator implements Comparator, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int compare(Comparable o1, Comparable o2) {
		return o1.compareTo(o2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		
		Object oo1 = o1;
		Object oo2 = o2;
		if (o2 instanceof OID) {
			OID oid = (OID) o2;
			o2 = oid.toByte();
		}
		if (o1 instanceof OID) {
			OID oid = (OID) o1;
			o1 = oid.toByte();
		}

		return compareByteArrays((byte[]) o1, (byte[]) o2);
	}

	/**
	 * Arrays should always be of the same size
	 * 
	 * @param b1
	 * @param b2
	 * @return
	 */
	public static int compareByteArrays(byte[] b1, byte[] b2) {
		if (b1.length != b2.length) {
			return b1.length - b2.length;
		}
		for (int i = b1.length - 1; i >= 0; i--) {
			int i1 = b1[i]+256;
			int i2 = b2[i]+256;
			int r = i1-i2;
			if (r != 0) {
				return r;
			}
		}
		return 0;
	}
}
