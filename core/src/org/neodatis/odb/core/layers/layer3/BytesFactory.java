/**
 * 
 */
package org.neodatis.odb.core.layers.layer3;

/**
 * @author olivier
 *
 */
public class BytesFactory {
	public static Bytes getBytes(){
		return new BytesImpl2();
	}
//	public static Bytes getBytes(long offset){
//		return new BytesImpl2(offset);
//	}

	/**
	 * @param byteArray
	 * @return
	 */
	public static Bytes getBytes(byte[] byteArray) {
		return new BytesImpl2(byteArray);
	}
	

	/**
	 * @param byteArray
	 * @return
	 */
//	public static Bytes getBytes(byte[] byteArray, long offset) {
//		return new BytesImpl2(byteArray,offset);
//	}
//	
	/**
	 *  
	 * @param byteArray
	 * @param offset
	 * @param deltaOffset See ByteImpl.deltaOffset javadoc
	 * @return
	 */
//	public static Bytes getBytes(byte[] byteArray, long offset, int deltaOffset) {
//		return new BytesImpl2(byteArray,offset, deltaOffset);
//	}

}
