package org.neodatis.odb.core.layers.layer3;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.ObjectOid;

public interface DataConverter {

	// values to byte array
	public abstract int booleanToByteArray(boolean b, Bytes arrayWhereToWrite, int offset, String label);

	public abstract int shortToByteArray(short s, Bytes arrayWhereToWrite, int offset, String lable);

	public abstract int charToByteArray(char c, Bytes arrayWhereToWrite, int offset, String label);

	public abstract int stringToByteArray(String s, boolean withEncoding, Bytes arrayWhereToWrite, int offset, String label);

	public abstract int bigDecimalToByteArray(BigDecimal bigDecimal, Bytes arrayWhereToWrite, int offset, String label);

	public abstract int bigIntegerToByteArray(BigInteger bigInteger, Bytes arrayWhereToWrite, int offset, String label);

	public abstract int dateToByteArray(Date date, Bytes arrayWhereToWrite, int offset, String label);

	public abstract int longToByteArray(long l, Bytes arrayWhereToWrite, int offset, String label);
	public abstract int longToByteArray(long classId, byte[] b, int offset, String label);

	public abstract boolean byteArrayToBoolean(Bytes bytes, int offset, ReadSize readSize, String label);
	public abstract byte byteArrayToByte(Bytes bytes, int offset, ReadSize readSize, String label);

	public abstract int intToByteArray(int l, Bytes arrayWhereToWrite, int offset, String label);
	public abstract int intToByteArray(int l, byte[] arrayWhereToWrite, int offset, String label);

	public abstract int getNumberOfBytesOfAString(String s, boolean useEncoding);
	public abstract int doubleToByteArray(double d, Bytes arrayWhereToWrite, int offset, String label);
	public abstract int byteToByteArray(byte b, Bytes arrayWhereToWrite, int offset, String label);
	public abstract int objectOidToByteArray(ObjectOid ooid, Bytes arrayWhereToWrite, int offset,String label);
	public abstract int classOidToByteArray(ClassOid coid, Bytes arrayWhereToWrite, int offset,String label);
	public abstract int floatToByteArray(float f, Bytes arrayWhereToWrite, int offset, String label);
	
	// byte array to value
	public abstract short byteArrayToShort(Bytes bytes, int offset, ReadSize readSize, String label);

	public abstract char byteArrayToChar(Bytes bytes,int offset, ReadSize readSize, String label);

	public abstract String byteArrayToString(Bytes bytes, boolean useEncoding, int offset, ReadSize readSize, String label);
	public abstract BigDecimal byteArrayToBigDecimal(Bytes bytes, int offset, ReadSize readSize, String label);
	public abstract BigInteger byteArrayToBigInteger(Bytes bytes, int offset, ReadSize readSize, String label);
	public abstract int byteArrayToInt(Bytes bytes, int offset, ReadSize readSize, String label);
	public abstract int byteArrayToInt(byte[] bb, int offset, String label);
	
	public abstract long byteArrayToLong(Bytes bytes, int offset, ReadSize readSize, String label);
	public abstract long byteArrayToLong(byte[] bb, int offset, String label);
	public abstract Date byteArrayToDate(Bytes bytes, int offset, ReadSize readSize, String label);
	public abstract float byteArrayToFloat(Bytes bytes, int offset, ReadSize readSize,String label);
	public abstract double byteArrayToDouble(Bytes bytes, int offset, ReadSize readSize, String label);
	public abstract ObjectOid byteArrayToObjectOid(Bytes bytes, int offset, ReadSize readSize, String label);
	public abstract ClassOid byteArrayToClassOid(Bytes bytes, int offset, ReadSize readSize, String label);
	
	public abstract void setDatabaseCharacterEncoding(String databaseCharacterEncoding);

	public void testEncoding(String encoding) throws UnsupportedEncodingException;

	

	

	

	

	

}