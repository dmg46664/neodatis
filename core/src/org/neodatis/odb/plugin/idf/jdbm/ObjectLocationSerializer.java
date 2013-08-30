/**
 * 
 */
package org.neodatis.odb.plugin.idf.jdbm;

import java.io.IOException;

import jdbm.helper.Serializer;

import org.neodatis.odb.plugin.idf.ObjectLocation;

/**
 * @author olivier
 *
 */
public class ObjectLocationSerializer implements Serializer {

	public Object deserialize(byte[] data) throws IOException {
		return ObjectLocation.fromBytes(data);
	}

	public byte[] serialize(Object obj) throws IOException {
		ObjectLocation ol = (ObjectLocation) obj;
		return ol.toBytes();
	}

}
