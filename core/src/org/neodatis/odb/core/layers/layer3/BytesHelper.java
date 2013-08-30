/**
 * 
 */
package org.neodatis.odb.core.layers.layer3;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.ObjectOid;

/**
 * @author olivier
 *
 */
public class BytesHelper {
	protected Bytes bytes;
	protected DataConverter converter;
	protected ReadSize internalReadSize;
	public BytesHelper(Bytes bytes, boolean debug, String characterEncoding, NeoDatisConfig config){
		this.bytes = bytes;
		this.converter = new DataConverterImpl(debug,characterEncoding, config);
		this.internalReadSize  = new ReadSize();
	}
	
	public BytesHelper(Bytes bytes, boolean debug, NeoDatisConfig config){
		this.bytes = bytes;
		this.converter = new DataConverterImpl(debug,null,config);
		this.internalReadSize  = new ReadSize();
	}
	public BytesHelper(Bytes bytes, NeoDatisConfig config){
		this.bytes = bytes;
		this.converter = new DataConverterImpl(false,null, config);
		this.internalReadSize  = new ReadSize();
	}
	public void resetReadSize(){
		internalReadSize = new ReadSize();
	}
	
	public ReadSize getReadSize(){
		return internalReadSize;
	}
	public int writeBigDecimal(BigDecimal bigDecimal, int offset, String label) {
		return converter.bigDecimalToByteArray(bigDecimal, bytes, offset, label);
	}
	public int writeBigInteger(BigInteger bigInteger, int offset, String label) {
		return converter.bigIntegerToByteArray(bigInteger, bytes, offset, label);
	}
	public int writeBoolean(boolean b, int offset, String label) {
		return converter.booleanToByteArray(b, bytes, offset, label);
	}
	public BigDecimal readbigDecimal(int offset, ReadSize readSize, String label) {
		return converter.byteArrayToBigDecimal(bytes, offset, readSize, label);
	}
	public BigDecimal readbigDecimal(int offset) {
		return converter.byteArrayToBigDecimal(bytes, offset, internalReadSize, "");
	}

	public BigInteger readBigInteger(int offset, ReadSize readSize, String label) {
		return converter.byteArrayToBigInteger(bytes, offset, readSize, label);
	}
	public BigInteger readBigInteger(int offset) {
		return converter.byteArrayToBigInteger(bytes, offset, internalReadSize, "");
	}

	public boolean readBoolean(int offset, ReadSize readSize, String label) {
		return converter.byteArrayToBoolean(bytes, offset, readSize, label);
	}

	public boolean readBoolean(int offset) {
		return converter.byteArrayToBoolean(bytes, offset, internalReadSize, "");
	}

	public byte readByte(int offset, ReadSize readSize, String label) {
		return converter.byteArrayToByte(bytes, offset, readSize, label);
	}

	public byte readByte(int offset) {
		return converter.byteArrayToByte(bytes, offset, internalReadSize, "");
	}

	public char readChar(int offset, ReadSize readSize, String label) {
		return converter.byteArrayToChar(bytes, offset, readSize, label);
	}

	public char readChar(int offset) {
		return converter.byteArrayToChar(bytes, offset, internalReadSize, "");
	}

	public ClassOid readClassOid(int offset, ReadSize readSize, String label) {
		return converter.byteArrayToClassOid(bytes, offset, readSize, label);
	}
	public ClassOid readClassOid(int offset) {
		return converter.byteArrayToClassOid(bytes, offset, internalReadSize, "");
	}
	public Date readDate( int offset) {
		return converter.byteArrayToDate(bytes, offset, internalReadSize,"");
	}
	public Date readDate( int offset, ReadSize readSize, String label) {
		return converter.byteArrayToDate(bytes, offset, readSize, label);
	}

	public double readDouble(int offset, ReadSize readSize, String label) {
		return converter.byteArrayToDouble(bytes, offset, readSize, label);
	}
	public double readDouble(int offset) {
		return converter.byteArrayToDouble(bytes, offset, internalReadSize, "");
	}

	public float readFloat(int offset, ReadSize readSize, String label) {
		return converter.byteArrayToFloat(bytes, offset, readSize, label);
	}
	public float readFloat(int offset) {
		return converter.byteArrayToFloat(bytes, offset, internalReadSize, "");
	}
	public int readInt(int offset, ReadSize readSize, String label) {
		return converter.byteArrayToInt(bytes, offset, readSize, label);
	}
	public int readInt(int offset) {
		return converter.byteArrayToInt(bytes, offset,internalReadSize,"");
	}
	public long readLong(int offset, ReadSize readSize, String label) {
		return converter.byteArrayToLong(bytes, offset, readSize, label);
	}
	public long readLong(int offset) {
		return converter.byteArrayToLong(bytes, offset,internalReadSize, "");
	}

	public ObjectOid readObjectoid( int offset, ReadSize readSize, String label) {
		return converter.byteArrayToObjectOid(bytes, offset, readSize, label);
	}
	public ObjectOid readObjectoid( int offset) {
		return converter.byteArrayToObjectOid(bytes, offset, internalReadSize, "");
	}

	public short readShort(int offset, ReadSize readSize, String label) {
		return converter.byteArrayToShort(bytes, offset, readSize, label);
	}
	public short readShort(int offset) {
		return converter.byteArrayToShort(bytes, offset, internalReadSize, "");
	}
	public String readString( boolean useEncoding, int offset, ReadSize readSize, String label) {
		return converter.byteArrayToString(bytes, useEncoding, offset, readSize, label);
	}
	public String readString( boolean useEncoding, int offset) {
		return converter.byteArrayToString(bytes, useEncoding, offset, internalReadSize, "");
	}
	public int writeByte(byte b, int offset, String label) {
		return converter.byteToByteArray(b, bytes, offset, label);
	}
	public int writeChar(char c, int offset, String label) {
		return converter.charToByteArray(c, bytes, offset, label);
	}
	public int writeClassOid(ClassOid coid, int offset, String label) {
		return converter.classOidToByteArray(coid, bytes, offset, label);
	}
	public int writeDate(Date date, int offset, String label) {
		return converter.dateToByteArray(date, bytes, offset, label);
	}
	public int writeDouble(double d, int offset, String label) {
		return converter.doubleToByteArray(d, bytes, offset, label);
	}
	public int writeFloat(float f, int offset, String label) {
		return converter.floatToByteArray(f, bytes, offset, label);
	}
	public int writeInt(int l, int offset, String label) {
		return converter.intToByteArray(l, bytes, offset, label);
	}
	public int writeLong(long l, int offset, String label) {
		return converter.longToByteArray(l, bytes, offset, label);
	}
	public int writeObjectOid(ObjectOid ooid, int offset, String label) {
		return converter.objectOidToByteArray(ooid, bytes, offset, label);
	}
	public int writeShort(short s, int offset, String lable) {
		return converter.shortToByteArray(s, bytes, offset, lable);
	}
	public int writeString(String s, boolean withEncoding, int offset, String label) {
		return converter.stringToByteArray(s, withEncoding, bytes, offset, label);
	}
	
	public Bytes getBytes(){
		return bytes;
	}
	/**
	 * @param bytes2
	 * @param position
	 * @param string
	 * @return
	 */
	public int writeBytes(Bytes bytes2Copy, int offset, String label) {
		bytes.set(offset, bytes2Copy.getByteArray());
		return bytes2Copy.getRealSize();
	}
	/**
	 * @param i
	 * @param readSize
	 * @param string
	 */
	public Bytes readBytes(int offset, int size, ReadSize readSize, String string) {
		byte[] nbytes = bytes.extract(offset, size);
		readSize.add(size);
		return BytesFactory.getBytes(nbytes);
		
	}

	/**Appends a array of byes. Only size bytes are appended
	 * @param b
	 * @param size
	 * @return
	 */
	public int appendByteArray(byte[] b, int size) {
		byte [] b2 = b;
		if(size!=b.length){
			b2 = new byte[size];
			System.arraycopy(b, 0, b2, 0, size);
		}
		bytes.append(b2);
		return bytes.getRealSize();
		
	}
}
