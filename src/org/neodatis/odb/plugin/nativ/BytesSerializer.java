/**
 * 
 */
package org.neodatis.odb.plugin.nativ;

import java.io.IOException;

import jdbm.helper.Serializer;

import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;

/**
 * @author olivier
 *
 */
public class BytesSerializer implements Serializer {

	public Object deserialize(byte[] data) throws IOException {
		return BytesFactory.getBytes(data);
	}

	public byte[] serialize(Object obj) throws IOException {
		Bytes bytes = (Bytes) obj;
		return bytes.getByteArray();
	}

}
