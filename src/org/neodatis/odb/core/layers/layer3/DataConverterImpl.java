
/*
NeoDatis ODB : Native Object Database (odb.info@neodatis.org)
Copyright (C) 2007 NeoDatis Inc. http://www.neodatis.org

"This file is part of the NeoDatis ODB open source object database".

NeoDatis ODB is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

NeoDatis ODB is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package org.neodatis.odb.core.layers.layer3;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer2.meta.ODBType;
import org.neodatis.odb.core.layers.layer4.OidGenerator;
import org.neodatis.tool.DLogger;


/**Converts array of bytes into native objects and native objects into array of bytes
 * 
 * @sharpen.ignore
 * 
 * @author osmadja
 *
 */
public class DataConverterImpl implements DataConverter {

	/** The encoding used for string to byte conversion*/
	private String encoding;
	private boolean hasEncoding;
	private static final byte BYTE_FOR_TRUE = 1;
	private static final byte BYTE_FOR_FALSE = 0;
	private static int INT_SIZE = DataConverterUtil.INT_SIZE;
	private static int LONG_SIZE = DataConverterUtil.LONG_SIZE;
	private static boolean debug;
	
	protected byte[] intBytes;
	protected byte[] longBytes;
	
	protected OidGenerator oidGenerator;

	public DataConverterImpl(boolean debug, String characterEncoding, NeoDatisConfig config){
		init(debug,characterEncoding, config);
	}
	public void init(boolean debug, String characterEncoding, NeoDatisConfig config) {
		INT_SIZE = ODBType.INTEGER.getSize();
		LONG_SIZE = ODBType.LONG.getSize();
		setDatabaseCharacterEncoding(characterEncoding);
		this.debug = debug;
		oidGenerator = config.getCoreProvider().getOidGenerator();
	}

	public  int booleanToByteArray(boolean b, Bytes arrayWhereToWrite, int offset, String label) {
		
		if(b){
			arrayWhereToWrite.set(offset,BYTE_FOR_TRUE);
		}else{
			arrayWhereToWrite.set(offset,BYTE_FOR_FALSE);
		}
		if(debug){
			DLogger.debug(String.format("    writing at %d : boolean '%b' : %s",offset+ arrayWhereToWrite.getOffset(),b,label));
		}
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#byteArrayToBoolean(byte[])
	 */
	public  boolean byteArrayToBoolean(Bytes bytes, int offset, ReadSize readSize, String label) {
		boolean b = false;
		if (bytes.get(offset) == 0) {
            bytes = null;
			b = false;
		}else{
			b = true;
		}
        readSize.add(1);
        
        if(debug){
        	DLogger.debug(String.format("     reading at %d : bool '%b' : %s",offset,b,label));	
        }
		return b;
	}
	public  byte byteArrayToByte(Bytes bytes, int offset, ReadSize readSize, String label) {
        readSize.add(1);
		byte b = bytes.get(offset);
		if(debug){
			DLogger.debug(String.format("     reading at %d : byte '%d' : %s",offset, b,label));
		}

		return b;
	}
	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#shortToByteArray(short)
	 */
	public  int shortToByteArray(short s, Bytes arrayWhereToWrite, int offset, String label) {
		int i, shift;
		for (i = 0, shift = 8; i < 2; i++, shift -= 8) {
			arrayWhereToWrite.set(i+offset,(byte) (0xFF & (s >> shift)));
		}
		if(debug){
			DLogger.debug(String.format("    writing short %d at %d : %s",s,offset+ arrayWhereToWrite.getOffset(),label));
		}
		return 2;
	}
	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#byteArrayToShort(byte[])
	 */
	public  short byteArrayToShort(Bytes bytes, int offset , ReadSize readSize, String label)  {
		short result = 0;

		for (int i = 0; i < 2; i++) {
			result <<= 8; // left shift out the last byte
			result |= bytes.get(i+offset) & 0xFF; // OR in the new byte
		}
        readSize.add(2);
        if(debug){
			DLogger.debug(String.format("     reading short %d at %d : %s",result,offset,label));
		}
		return result;
	}


	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#charToByteArray(char)
	 */
	public  int charToByteArray(char c,  Bytes arrayWhereToWrite, int offset, String label){
		int i, shift;
		for (i = 0, shift = 8; i < 2; i++, shift -= 8) {
			arrayWhereToWrite.set(i+offset, (byte) (0xFF & (c >> shift)));
		}
		if(debug){
			DLogger.debug(String.format("    writing at %d : char '%c' : %s",offset+ arrayWhereToWrite.getOffset(),c,label));
		}
		return 2; 
		}
	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#byteArrayToChar(byte[])
	 */
	public  char byteArrayToChar(Bytes bytes, int offset, ReadSize readSize, String label) {
		char result = 0;

		for (int i = 0; i < 2; i++) {
			result <<= 8; // left shift out the last byte
			result |= bytes.get(i+offset) & 0xFF; // OR in the new byte
		}
		readSize.add(2);
        if(debug){
			DLogger.debug(String.format("     reading at %d : char %c : %s",offset,result,label));
		}
		return result;
	}

    /* (non-Javadoc)
	 * @see org.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#getNumberOfBytesOfAString(java.lang.String)
	 * FIXME use encoding
	 */
    public  int getNumberOfBytesOfAString(String s, boolean useEncoding){
        if(useEncoding&&hasEncoding){
        	try {
				return s.getBytes(encoding).length;
			} catch (UnsupportedEncodingException e) {
				throw new NeoDatisRuntimeException(org.neodatis.odb.core.NeoDatisError.UNSUPPORTED_ENCODING.addParameter(encoding));
			}
        }
        return s.getBytes().length;
    }
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#
	 * stringToByteArray(java.lang.String, boolean, int)
	 */
	public int stringToByteArray(String s, boolean withEncoding, Bytes bytesWhereToWrite, int offset,String label) {
		byte[] bytes = null;

		if (withEncoding && hasEncoding) {
			try {
				bytes = s.getBytes(encoding);
			} catch (UnsupportedEncodingException e) {
				throw new NeoDatisRuntimeException(e,e.getMessage());
			}
		} else {
			bytes = s.getBytes();
		}
		int totalSize = bytes.length;

		// copy the bytes of the total size
		intToByteArray(totalSize, bytesWhereToWrite, offset,label);
		// Copy the string data byte
		bytesWhereToWrite.copy(bytes,0,offset+INT_SIZE,bytes.length,true);
		if(debug){
			DLogger.debug(String.format("    writing at %d : string '%s' : %s",offset+ bytesWhereToWrite.getOffset(),s,label));
		}

		return totalSize+INT_SIZE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#
	 * byteArrayToString(byte[], boolean)
	 */
	public String byteArrayToString(Bytes bytes, boolean useEncoding, int offset, ReadSize readSize, String label) {
		String s = null;
		int realSize = byteArrayToInt(bytes, offset,readSize,label);

		byte[] rbytes = bytes.extract(offset+INT_SIZE, realSize);
		if (useEncoding && hasEncoding) {
			try {
				s = new String(rbytes, 0, realSize, encoding);
			} catch (UnsupportedEncodingException e) {
				throw new NeoDatisRuntimeException(e,e.getMessage());
			}
		} else {
			s = new String(rbytes, 0, realSize);
		}
		readSize.add(realSize);
		bytes = null;
		
		if(debug){
			DLogger.debug(String.format("     reading at %d : string '%s' : %s",offset,s,label));
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#
	 * bigDecimalToByteArray(java.math.BigDecimal, boolean)
	 */
	public int bigDecimalToByteArray(BigDecimal bigDecimal, Bytes bytesWhereToWrite, int offset, String label) {
		return stringToByteArray(bigDecimal.toString(), false, bytesWhereToWrite, offset,label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#
	 * byteArrayToBigDecimal(byte[], boolean)
	 */
	public BigDecimal byteArrayToBigDecimal(Bytes bytes, int offset, ReadSize readSize, String label) {
		String s = byteArrayToString(bytes, false, offset, readSize,label);
		return new BigDecimal(s);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#
	 * bigIntegerToByteArray(java.math.BigInteger, boolean)
	 */
	public int bigIntegerToByteArray(BigInteger bigInteger, Bytes bytesWhereToWrite, int offset, String label) {
		return stringToByteArray(bigInteger.toString(), false, bytesWhereToWrite, offset,label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#
	 * byteArrayToBigInteger(byte[], boolean)
	 */
	public BigInteger byteArrayToBigInteger(Bytes bytes, int offset, ReadSize readSize, String label) {
		String s = byteArrayToString(bytes, false, offset, readSize,label);
		return new BigInteger(s);
	}


	/**
	 * This method writes the byte directly to the array identification
	 */
	public  int intToByteArray2(int l, Bytes arrayWhereToWrite, int offset, String label)  {
		int i, shift;
		for (i = 0, shift = 24; i < 4; i++, shift -= 8) {
			arrayWhereToWrite.set(offset+i, (byte) (0xFF & (l >> shift)));
		}
		if(debug){
			DLogger.debug(String.format("    writing at %d : int '%d' : %s",offset,l,label));
		}

		return INT_SIZE;
	}

	/**
	 * This method writes the byte directly to the array identification
	 */
	public  int intToByteArray(int l, Bytes arrayWhereToWrite, int offset, String label)  {
		int i, shift;
		if(intBytes==null){
			intBytes = new byte[INT_SIZE];
		}
		//mbytes = new byte[4];
		for (i = 0, shift = 24; i < 4; i++, shift -= 8) {
			//arrayWhereToWrite.set(offset+i, (byte) (0xFF & (l >> shift)));
			intBytes[i] = (byte) (0xFF & (l >> shift));
		}
		arrayWhereToWrite.set(offset, intBytes);
		if(debug){
			DLogger.debug(String.format("    writing at %d : int '%d' : %s",offset+ arrayWhereToWrite.getOffset(),l,label));
		}

		return INT_SIZE;
	}
	public int intToByteArray(int l, byte[] arrayWhereToWrite, int offset, String label) {
		return DataConverterUtil.intToByteArray(l, arrayWhereToWrite, offset, label,debug);
	}


	/**
	 * This method writes the byte directly to the array identification
	 */
	public  int byteToByteArray(byte b, Bytes arrayWhereToWrite, int offset, String label)  {
		arrayWhereToWrite.set(offset,b);
		if(debug){
			DLogger.debug(String.format("    writing at %d : byte '%d' : %s",offset + arrayWhereToWrite.getOffset(),b,label));
		}
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#byteArrayToInt(byte[], int)
	 */
	public  int byteArrayToInt(Bytes bytes, int offset, ReadSize readSize, String label)  {
		int result = 0;
		if(intBytes==null){
			intBytes = new byte[INT_SIZE];
		}
		intBytes = bytes.get(offset, intBytes, 0, 4);
		for (int i = 0; i < 4; i++) {
			result <<= 8; // left shift out the last byte
			result |= intBytes[i] & 0xFF; // OR in the new byte
		}
		readSize.add(4);
        bytes = null;
        if(debug){
			DLogger.debug(String.format("     reading at %d : int '%d' : %s",offset , result,label));
		}
		return result;
	}
	public  int byteArrayToInt(byte[] bytes, int offset, String label)  {
		return DataConverterUtil.byteArrayToInt(bytes, offset, label, debug);
	}

	public  int byteArrayToInt2(Bytes bytes, int offset, ReadSize readSize, String label)  {
		int result = 0;
		
		for (int i = 0; i < 4; i++) {
			result <<= 8; // left shift out the last byte
			result |= bytes.get(i+offset) & 0xFF; // OR in the new byte
		}
		readSize.add(4);
        bytes = null;
        if(debug){
			DLogger.debug(String.format("     reading at %d : int '%d' : %s",offset , result,label));
		}
		return result;
	}

	
	/**
	 * This method writes the byte directly to the array identification
	 */
	public  int longToByteArray2(long l, Bytes arrayWhereToWrite, int offset, String label)  {
		int i, shift;
		for (i = 0, shift = 56; i < 8; i++, shift -= 8) {
			arrayWhereToWrite.set(offset+i, (byte) (0xFF & (l >> shift)));
		}
		if(debug){
			DLogger.debug(String.format("    writing at %d : long '%d' : %s",offset,l,label));
		}
		
		return LONG_SIZE;
	}
	/**
	 * This method writes the byte directly to the array identification
	 */
	public  int longToByteArray(long l, Bytes arrayWhereToWrite, int offset, String label)  {
		int i, shift;
		if(longBytes==null){
			longBytes = new byte[LONG_SIZE];
		}
		for (i = 0, shift = 56; i < 8; i++, shift -= 8) {
			//arrayWhereToWrite.set(offset+i, (byte) (0xFF & (l >> shift)));
			longBytes[i] = (byte) (0xFF & (l >> shift));
		}
		arrayWhereToWrite.set(offset, longBytes);
		if(debug){
			DLogger.debug(String.format("    writing at %d : long '%d' : %s",offset+ arrayWhereToWrite.getOffset(),l,label));
		}
		
		return LONG_SIZE;
	}
	public  int longToByteArray(long l, byte[] arrayWhereToWrite, int offset, String label)  {
		return DataConverterUtil.longToByteArray(l, arrayWhereToWrite, offset, label, debug);
	}
	
	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#byteArrayToLong(byte[])
	 */
	public  long byteArrayToLong(Bytes bytes, int offset, ReadSize readSize, String label)  {
		//return ByteBuffer.wrap(bytes).getLong();
		long result = 0;
		if(longBytes==null){
			longBytes = new byte[LONG_SIZE];
		}
		longBytes = bytes.get(offset, longBytes, 0, 8);
		for (int i = 0; i < 8; i++) {
			result <<= 8; // left shift out the last byte
			result |= longBytes[i] & 0xFF; // OR in the new byte			
		}
		readSize.add(8);
        bytes = null;
        if(debug){
			DLogger.debug(String.format("     reading at %d : long %d : %s",offset,result,label));
		}
		return result;
	}
	public  long byteArrayToLong(byte[] bytes, int offset, String label)  {
		return DataConverterUtil.byteArrayToLong(bytes, offset, label, debug);
	}
	public  long byteArrayToLong2(Bytes bytes, int offset, ReadSize readSize, String label)  {
		
		long result = 0;

		for (int i = 0; i < 8; i++) {
			result <<= 8; // left shift out the last byte
			result |= bytes.get(i+offset) & 0xFF; // OR in the new byte			
		}
		readSize.add(8);
        bytes = null;
        if(debug){
			DLogger.debug(String.format("     reading at %d : long %d : %s",offset,result,label));
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#dateToByteArray(java.util.Date)
	 */
	public  int dateToByteArray(Date date, Bytes arrayWhereToWrite, int offset,String label)  {
		return longToByteArray(date.getTime(),arrayWhereToWrite,offset,label);
	}
	
	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#byteArrayToDate(byte[])
	 */
	public  Date byteArrayToDate(Bytes bytes,int offset, ReadSize readSize, String label){
		return new Date(byteArrayToLong(bytes,offset,readSize,label));
	}
	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#floatToByteArray(float)
	 */
	public  int floatToByteArray(float f, Bytes arrayWhereToWrite, int offset, String label) {
		int i = Float.floatToIntBits(f);
		return intToByteArray(i,arrayWhereToWrite,offset,label);
	}
	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#byteArrayToFloat(byte[])
	 */
	public  float byteArrayToFloat(Bytes bytes, int offset, ReadSize readSize, String label){
		return Float.intBitsToFloat(byteArrayToInt(bytes,offset,readSize,label));
	}

	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#doubleToByteArray(double)
	 */
	public  int doubleToByteArray(double d, Bytes arrayWhereToWrite, int offset, String label) {
		long i = Double.doubleToLongBits(d);
		return longToByteArray(i,arrayWhereToWrite,offset,label);
	}
	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.impl.layers.layer3.engine.IByteArrayConverter#byteArrayToDouble(byte[])
	 */
	public  double byteArrayToDouble(Bytes bytes, int offset, ReadSize readSize, String label)  {
		return Double.longBitsToDouble(byteArrayToLong(bytes,offset,readSize,label));
	}

	public void setDatabaseCharacterEncoding(String databaseCharacterEncoding) {
		encoding = databaseCharacterEncoding;
		if(encoding==null){
			hasEncoding=false;
		}else{
			hasEncoding=true;
		}
		
	}

	public void testEncoding(String encoding) throws UnsupportedEncodingException {
		"test encoding".getBytes(encoding);
	}

	public ObjectOid byteArrayToObjectOid(Bytes bytes, int offset, ReadSize readSize, String label) {
		int size = byteArrayToInt(bytes, offset, readSize, "oid bytes size");
		byte[]bb = bytes.extract(offset+INT_SIZE, size);
		ObjectOid oid = oidGenerator.buildObjectOID(bb);
		readSize.add(size);

		if(debug){
			DLogger.debug(String.format("     reading at %d : object oid '%s' ",offset,oid, label));
		}

		return oid;
	}

	public ClassOid byteArrayToClassOid(Bytes bytes, int offset, ReadSize readSize, String label) {
		int size = byteArrayToInt(bytes, offset, readSize, "oid bytes size");
		byte[]bb = bytes.extract(offset+INT_SIZE, size);
		ClassOid oid = oidGenerator.buildClassOID(bb);
		readSize.add(size);

		if(debug){
			DLogger.debug(String.format("     reading at %d : class oid '%s' ",offset,oid, label));
		}

		return oid;
	}


	public int oidToByteArray(OID ooid, Bytes arrayWhereToWrite, int offset, String label) {
		
		if (ooid == null || ooid.isNull()) {
			if(debug){
				DLogger.debug(String.format("    writing at %d : null object oid : %s",offset+ arrayWhereToWrite.getOffset(),label));
			}
			return intToByteArray(0, arrayWhereToWrite, offset, "null oid");
		} else {
			if(debug){
				DLogger.debug(String.format("    writing at %d : object oid '%s' : %s",offset+ arrayWhereToWrite.getOffset(),ooid.toString(),label));
			}
			byte[] bb = ooid.toByte();
			intToByteArray(bb.length, arrayWhereToWrite, offset, "oid bytes size");
			arrayWhereToWrite.set(offset+INT_SIZE, bb);
			return bb.length+INT_SIZE;
		}	
	}

	
	public int objectOidToByteArray(ObjectOid ooid, Bytes arrayWhereToWrite, int offset, String label) {
		if(ooid==null){
			ooid = oidGenerator.getNullObjectOid();
		}
		return oidToByteArray(ooid, arrayWhereToWrite, offset, label);
	}
	
	public int classOidToByteArray(ClassOid coid, Bytes arrayWhereToWrite, int offset, String label) {
		if(coid==null){
			coid = oidGenerator.getNullClassOid();
		}
		return oidToByteArray(coid, arrayWhereToWrite, offset, label);
	}

	
}