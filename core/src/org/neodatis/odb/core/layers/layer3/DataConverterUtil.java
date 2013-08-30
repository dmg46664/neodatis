package org.neodatis.odb.core.layers.layer3;

import org.neodatis.tool.DLogger;

public class DataConverterUtil {
	
	public static int INT_SIZE = 4;
	public static int LONG_SIZE = 8;

	public static int intToByteArray(int l, byte[] arrayWhereToWrite, int offset, String label, boolean debug) {
		int i, shift;
		// mbytes = new byte[4];
		for (i = 0, shift = 24; i < 4; i++, shift -= 8) {
			// arrayWhereToWrite.set(offset+i, (byte) (0xFF & (l >> shift)));
			arrayWhereToWrite[offset + i] = (byte) (0xFF & (l >> shift));
		}

		if (debug) {
			DLogger.debug(String.format("    writing at %d : int '%d' : %s", offset, l, label));
		}

		return INT_SIZE;
	}
	
	public static int longToByteArray(long l, byte[] arrayWhereToWrite, int offset, String label, boolean debug)  {
		int i, shift;
		
		for (i = 0, shift = 56; i < 8; i++, shift -= 8) {
			//arrayWhereToWrite.set(offset+i, (byte) (0xFF & (l >> shift)));
			arrayWhereToWrite[i+offset] = (byte) (0xFF & (l >> shift));
		}
		if(debug){
			DLogger.debug(String.format("    writing at %d : long '%d' : %s",offset,l,label));
		}
		
		return LONG_SIZE;
	}

	public static long byteArrayToLong(byte[] bytes, int offset, String label, boolean debug)  {
		//return ByteBuffer.wrap(bytes).getLong();
		long result = 0;
		for (int i = 0; i < 8; i++) {
			result <<= 8; // left shift out the last byte
			result |= bytes[offset+i] & 0xFF; // OR in the new byte			
		}
        if(debug){
			DLogger.debug(String.format("     reading at %d : long %d : %s",offset,result,label));
		}
		return result;
	}

	public static int byteArrayToInt(byte[] bytes, int offset, String label, boolean debug)  {
		int result = 0;
		for (int i = 0; i < 4; i++) {
			result <<= 8; // left shift out the last byte
			result |= bytes[offset+i] & 0xFF; // OR in the new byte
		}
        if(debug){
			DLogger.debug(String.format("     reading at %d : int '%d' : %s",offset , result,label));
		}
		return result;
	}


}
