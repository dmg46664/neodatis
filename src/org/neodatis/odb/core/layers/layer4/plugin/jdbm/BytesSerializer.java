/**
 * 
 */
package org.neodatis.odb.core.layers.layer4.plugin.jdbm;

import java.io.IOException;

import jdbm.helper.Serializer;

import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesImpl;

/**
 * @author olivier
 *
 */
public class BytesSerializer implements Serializer {

	/* (non-Javadoc)
	 * @see jdbm.extser.ISimpleSerializer#deserialize(byte[])
	 */
	public Object deserialize(byte[] data) throws IOException {
		return BytesFactory.getBytes(data);
	}

	public byte[] serialize(Object obj) throws IOException {
		Bytes bytes = (Bytes) obj;
		return bytes.getByteArray();
	}

}
