/**
 * 
 */
package org.neodatis.odb.core.layers.layer4.plugin.jdbm;

import java.io.IOException;

import org.neodatis.odb.OID;

import jdbm.helper.Serializer;

/**
 * @author olivier
 *
 */
public class KeySerializer implements Serializer {

	/* (non-Javadoc)
	 * @see jdbm.extser.ISimpleSerializer#deserialize(byte[])
	 */
	public Object deserialize(byte[] data) throws IOException {
		return data;
	}

	public byte[] serialize(Object obj) throws IOException {
		//System.out.println("Serializing object " + obj + " of type " + obj.getClass().getName());
		if(obj instanceof OID){
			OID oid = (OID) obj;
			return oid.toByte();
		}
		byte[] b = (byte[]) obj;
		return b;
	}

}
