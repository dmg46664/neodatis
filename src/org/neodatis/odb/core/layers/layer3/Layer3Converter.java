package org.neodatis.odb.core.layers.layer3;

import org.neodatis.odb.core.layers.layer2.meta.ODBType;

public class Layer3Converter {

	public static final int BYTE_SIZE = ODBType.BYTE.getSize();
	public static final int CHAR_SIZE = ODBType.CHARACTER.getSize();
	public static final int INT_SIZE = ODBType.INTEGER.getSize();
	public static final int LONG_SIZE = ODBType.LONG.getSize();
	public static final int NATIVE_HEADER_BLOCK_SIZE = ODBType.INTEGER.getSize() + ODBType.BYTE.getSize() + ODBType.INTEGER.getSize()
			+ ODBType.BOOLEAN.getSize();

	
	
	public static final int POS_OBJECT_CHECKSUM1 = 0;
	public static final int POS_OBJECT_OBJECT_SIZE = 4;
	public static final int POS_OBJECT_OBJECT_TYPE = 12;
	public static final int POS_OBJECT_OID = 16;
	public static final int POS_OBJECT_CLASS_OID = 48;
	public static final int POS_OBJECT_CREATION_DATE = 80;
	public static final int POS_OBJECT_UPDATE_DATE = 88;
	public static final int POS_OBJECT_OBJECT_VERSION = 96;
	public static final int POS_OBJECT_HAS_EXTERNAL_SYNC = 104;
	public static final int POS_OBJECT_NB_ATTRIBUTES = 105;
	public static final int POS_OBJECT_ATTRIBUTE_HEADER_SIZE = 109;
	public static final int POS_OBJECT_ATTRIBUTE_DEFINITION = 113;
	
	public static final int POS_CI_CHECKSUM1 = 0;
	public static final int POS_CI_OBJECT_SIZE = 4;
	public static final int POS_CI_OBJECT_TYPE = 8;
	public static final int POS_CI_CLASS_CATEGORY = 9;
	public static final int POS_CI_OID = 10;

	public static final byte ATTRIBUTE_IS_NATIVE = 1;
	public static final byte ATTRIBUTE_IS_NON_NATIVE = 2;
	public static byte[] NATIVE_HEADER_BLOCK_SIZE_BYTE = null;

	public static final int DEFAULT_BYTES_SIZE = 256;
	
	public Layer3Converter(){

	}
	
	
}

