/**
 * 
 */
package org.neodatis.odb.plugin.nativ;

import java.io.IOException;

import org.neodatis.odb.OID;

import jdbm.helper.Serializer;

/**
 * @author olivier
 *
 */
public class KeySerializer implements Serializer {

	public Object deserialize(byte[] data) throws IOException {
		return data;
	}

	public byte[] serialize(Object obj) throws IOException {
		if(obj instanceof OID){
			OID oid = (OID) obj;
			return oid.toByte();
		}
		byte[] b = (byte[]) obj;
		return b;
	}

}
