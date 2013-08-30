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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.CoreProvider;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer2.meta.AbstractObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.ArrayObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.AtomicNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.AttributeIdentification;
import org.neodatis.odb.core.layers.layer2.meta.ClassAttributeInfo;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.CollectionObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.EnumNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.MapObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeNullObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NullNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.ODBType;
import org.neodatis.odb.core.layers.layer2.meta.ObjectReference;
import org.neodatis.odb.core.session.DatabaseInfo;
import org.neodatis.odb.core.session.Session;
import org.neodatis.odb.core.trigger.TriggerManager;
import org.neodatis.tool.DLogger;
import org.neodatis.tool.DisplayUtility;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;

/**
 * Manage all IO writing
 * 
 * @author olivier s
 * 
 * 
 * 
 * 
 */
public class Layer3WriterImpl extends Layer3Converter implements Layer3Writer {

	public static final String LOG_ID = "Layer3Writer";
	public static final String LOG_ID_DEBUG = "Layer3Writer.debug";

	protected DataConverter byteArrayConverter;
	/** Is is used to convert class name to class oids */
	protected Session session;
	protected boolean debug;

	public Layer3WriterImpl(Session session) {
		super();
		this.session = session;
		CoreProvider provider = session.getConfig().getCoreProvider();

		this.byteArrayConverter = provider.getByteArrayConverter(session.getConfig().debugLayers(), session.getConfig()
				.getDatabaseCharacterEncoding(), session.getConfig());

		NATIVE_HEADER_BLOCK_SIZE_BYTE = new byte[4];
		Bytes b = new BytesImpl();
		byteArrayConverter.intToByteArray(NATIVE_HEADER_BLOCK_SIZE, b, 0, "init block size");
		NATIVE_HEADER_BLOCK_SIZE_BYTE = b.extract(0, 4);
		debug = session.getConfig().debugLayers();
	}

	public IOdbList<OidAndBytes> metaToBytes(NonNativeObjectInfo nnoi) {
		ConversionContext context = new ConversionContext();
		internalMetaToBytes(nnoi, context);
		return context.getOidAndBytes();
	}

	protected void internalMetaToBytes(NonNativeObjectInfo objectInfo, ConversionContext context) {

		boolean isMainObject = context.isMainObject();
		if (isMainObject) {
			context.setIsMainObject(false);
		}
		if (debug) {
			DLogger.debug(String.format("\n** Converting nnoi %s with oid %s to bytes", objectInfo.getClassInfo().getFullClassName(), objectInfo
					.getOid()));
		}

		// Checks if object is null,for null objects,there is nothing to do
		if (objectInfo.isNull()) {
			context.setLastOid(null);
			return;
		}

		ObjectOid existingOid = objectInfo.getOid();
		
		

		// Check if it has already been converted (to avoid cyclic references)
		if (context.alreadyConvertedOids.containsKey(objectInfo.getOid())) {
			context.setLastOid(objectInfo.getOid());
			return;
		}
		// Put in the map
		context.alreadyConvertedOids.put(existingOid, objectInfo);

		Bytes bytesContent = BytesFactory.getBytes();
		BytesHelper bytes = new BytesHelper(bytesContent, session.getConfig().debugLayers(), session.getConfig());

		IOdbList<NonNativeObjectInfo> dependentObjects = new OdbArrayList<NonNativeObjectInfo>();
		ClassInfo ci = objectInfo.getClassInfo();
		
		String fullClassName = ci.getFullClassName();
		TriggerManager tm = session.getEngine().getTriggerManager();
		if (existingOid.isNew() && tm.hasOidTriggersFor(fullClassName)) {
			tm.manageOidTrigger(fullClassName, objectInfo, existingOid);
		}


		
		

		int nbAttributes = objectInfo.getClassInfo().getAttributes().size();
		int offset = 0;
		int position = 0;
		int objectSize = 0;

		// Block type TODO we could keep these bytes ready
		objectSize += bytes.writeInt(BlockTypes.BLOCK_TYPE_NON_NATIVE_OBJECT, offset + objectSize, "object type");

		// The object id
		objectSize += bytes.writeObjectOid(objectInfo.getOid(), offset + objectSize, "oid");
		// creation date
		objectSize += bytes.writeLong(objectInfo.getHeader().getCreationDate(), offset + objectSize, "creation");
		// first add one to the object version and set update date to now
		objectInfo.getHeader().setObjectVersion(objectInfo.getHeader().getObjectVersion() + 1);
		objectInfo.getHeader().setUpdateDate(System.currentTimeMillis());

		// update date
		objectSize += bytes.writeLong(objectInfo.getHeader().getUpdateDate(), offset + objectSize, "update");
		// object version
		objectSize += bytes.writeLong(objectInfo.getHeader().getObjectVersion(), offset + objectSize, "version");
		// True if this object have been synchronized with main database, else
		// false
		objectSize += bytes.writeBoolean(false, offset + objectSize, "si ext sync");
		
		// Write 2 long just to keep spare space for future use
		objectSize += bytes.writeLong(0, offset + objectSize, "reserved1");
		objectSize += bytes.writeLong(0, offset + objectSize, "reserved2");
		
		// now write the number of attributes and the position of all
		// attributes, we do not know them yet, so we pull that part and These
		// data will be updated ate the end
		// for each attribute we write the attribute id as an int, then we write
		// a byte to indicate if object is native(1) or non native(2),
		// then for native object, we write the offset position and for non
		// native we write the oid

		// first write the number of attributes
		objectSize += bytes.writeInt(nbAttributes, offset + objectSize, "nb attributes");
		
		int headerPositionInfo = objectSize;
		//pull one int to put the header position
		objectSize += bytes.writeInt(0, offset + objectSize, "header position");
		
		AttributeIdentification[] attributeIdentifications = new AttributeIdentification[nbAttributes];

		// Get the number of non native attributes
		int nbNonNativeAttributes = objectInfo.getDirectNonNativeAttributes().size();
		int nbNativeAttributes = nbAttributes - nbNonNativeAttributes;

		ClassAttributeInfo cai = null;
		AbstractObjectInfo attributeValue = null;
		// We write values on another bytes instance that will be later appended
		// to the original
		// int attributePosition = 0;
		Bytes bytesAttributeValues = bytes.bytes;//;BytesFactory.getBytes();
		// this offset is only used for display reason (in debug, to display the right absolute position)
		//bytesAttributeValues.setOffset(objectSize);

		
		// Loop on all attributes of the class
		for (int i = 0; i < nbAttributes; i++) {
			// Gets the attribute meta description
			cai = ci.getAttributeInfo(i);
			// Gets the attribute data
			attributeValue = objectInfo.getAttributeValueFromId(cai.getId());

			if (attributeValue == null) {
				// This only happens in 1 case : when a class has a field with
				// the same name of one of is superclass. In this case , the
				// deeper attribute is null
				if (cai.isNative()) {
					attributeValue = new NullNativeObjectInfo(cai.getAttributeType().getId());
				} else {
					attributeValue = new NonNativeNullObjectInfo();
				}
			}
			// Native Objects
			if (attributeValue.isNative()) {
				AttributeIdentification ai = nativeObjectInfoToBytes((NativeObjectInfo) attributeValue, bytesAttributeValues,
						objectSize, context);
				objectSize += ai.size;
				// For native objects , odb stores their position, relative to
				// the start of the object(offset)
				attributeIdentifications[i] = ai;
			} else {
				// Non Native Objects
				ObjectOid attributeOid = null;
				if (attributeValue.isObjectReference()) {
					ObjectReference or = (ObjectReference) attributeValue;
					attributeOid = or.getOid();
				} else {
					NonNativeObjectInfo nnoi = (NonNativeObjectInfo) attributeValue;
					attributeOid = nnoi.getOid();
					if (!nnoi.isNull()) {
						dependentObjects.add(nnoi);
					}
				}
				attributeIdentifications[i] = new AttributeIdentification(attributeOid);
				objectSize += byteArrayConverter.objectOidToByteArray(attributeOid, bytesAttributeValues, objectSize, "oid");
			}
			// keep the attribute id
			attributeIdentifications[i].id = cai.getId();

		}

		// Updates attributes identification in the object info header
		objectInfo.getHeader().setAttributesIdentification(attributeIdentifications);

		//objectSize += attributesSize;
		
		if (debug) {
			DLogger.debug("  ** end writing attribute at " + objectSize);
		}

		// write the position where to find the header
		bytes.writeInt(objectSize, headerPositionInfo, "header position");
		
		// write attributes header (at the end)
		int asize = headerToBytes(attributeIdentifications, bytes.bytes, objectSize, true);
		
		objectSize += asize;

		// Write object size
		objectSize+= bytes.writeInt(objectSize, objectSize, "object size");
		// Write Crc
		int crc = computeCrc(bytes.bytes);
		objectSize+=bytes.writeInt(crc, objectSize, "crc");

		if (debug) {
			DLogger.debug(String.format("  Object with OID %s has a size of %d - crc=%d", existingOid, objectSize,crc));
			DLogger.debug("  Attributes Identification of object with oid " + existingOid + " are " + DisplayUtility.toString(attributeIdentifications));
			DLogger.debug("**End Writing non native object at " + position + " with oid " + existingOid);
		}

		context.addOidAndBytes(existingOid, bytes.bytes, objectInfo, isMainObject);

		context.setLastOid(existingOid);
		for (NonNativeObjectInfo nnoi : dependentObjects) {
			if (!nnoi.isNull()) {
				internalMetaToBytes(nnoi, context);
			}
		}

		if (debug) {
			DLogger.debug(String.format("End od Converting nnoi with oid " + objectInfo.getOid() + " to bytes, %d dependent objects",
					dependentObjects.size()));
		}

	}


	private int computeCrc(Bytes bytes) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * 
	 * @param attributeIdentifications
	 * @param bytes
	 *            Where to write
	 * @param offset
	 *            The offset position to start to write from
	 * @return The size of what has been written
	 */
	protected int headerToBytes(AttributeIdentification[] attributeIdentifications, Bytes bytes, int offset, boolean writeId) {
		// write attributes header
		int position = offset;
		int size = 0;
		int nbAttributes = attributeIdentifications.length;
		for (int i = 0; i < nbAttributes; i++) {
			AttributeIdentification ai = attributeIdentifications[i];
			if (ai.isNative) {
				if (writeId) {
					size += byteArrayConverter.intToByteArray(ai.id, bytes, position + size, "attribute id");
				}
				size += byteArrayConverter.byteToByteArray(ATTRIBUTE_IS_NATIVE, bytes, position + size, "is native");
				size += byteArrayConverter.intToByteArray(ai.offset, bytes, position + size, "attribute offset");
			} else {
				if (writeId) {
					size += byteArrayConverter.intToByteArray(ai.id, bytes, position + size, "attribute id");
				}
				size += byteArrayConverter.byteToByteArray(ATTRIBUTE_IS_NON_NATIVE, bytes, position + size, "is non native");
				size += byteArrayConverter.objectOidToByteArray(ai.oid, bytes, position + size, "oid");
			}
		}
		return size;
	}

	/**
	 * Write the header of a native attribute
	 * 
	 * @param odbTypeId
	 * @param isNull
	 * @param writeDataInTransaction
	 *            @
	 */
	protected int nativeObjectHeaderToBytes(int odbTypeId, boolean isNull, byte blockType, Bytes bytes, int offset) {
		// bytes.set(offset, NATIVE_HEADER_BLOCK_SIZE_BYTE[0]);
		// bytes.set(offset + 1, NATIVE_HEADER_BLOCK_SIZE_BYTE[1]);
		// bytes.set(offset + 2, NATIVE_HEADER_BLOCK_SIZE_BYTE[2]);
		// bytes.set(offset + 3, NATIVE_HEADER_BLOCK_SIZE_BYTE[3]);
		// bytes.set(offset + 4, blockType);
		byteArrayConverter.intToByteArray(NATIVE_HEADER_BLOCK_SIZE, bytes, offset, "block size");
		byteArrayConverter.byteToByteArray(blockType, bytes, offset + 4, "block type");
		byteArrayConverter.intToByteArray(odbTypeId, bytes, offset + 5, "odb type id");
		byteArrayConverter.booleanToByteArray(isNull, bytes, offset + 9, "is null?");
		return 10;
	}

	public AttributeIdentification atomicNativeObjectToBytes(AtomicNativeObjectInfo anoi, Bytes bytes, int offset) {
		int originalPosition = offset;
		int odbTypeId = anoi.getOdbTypeId();

		int objectSize = nativeObjectHeaderToBytes(odbTypeId, anoi.isNull(), BlockTypes.BLOCK_TYPE_NATIVE_OBJECT, bytes, offset);

		if (anoi.isNull()) {
			// The header already has the null info, no more need to write
			return new AttributeIdentification(offset, objectSize);
		}

		offset += objectSize;
		Object object = anoi.getObject();

		switch (odbTypeId) {
		case ODBType.BYTE_ID:
		case ODBType.NATIVE_BYTE_ID:
			objectSize += byteArrayConverter.byteToByteArray((Byte) object, bytes, offset, "byte value");
			break;
		case ODBType.BOOLEAN_ID:
		case ODBType.NATIVE_BOOLEAN_ID:
			objectSize += byteArrayConverter.booleanToByteArray((Boolean) object, bytes, offset, "boolean value");
			break;
		case ODBType.CHARACTER_ID:
			objectSize += byteArrayConverter.charToByteArray((Character) object, bytes, offset, "char value");
			break;
		case ODBType.NATIVE_CHAR_ID:
			objectSize += byteArrayConverter.charToByteArray(object.toString().charAt(0), bytes, offset, "char value");
			break;
		case ODBType.FLOAT_ID:
		case ODBType.NATIVE_FLOAT_ID:
			objectSize += byteArrayConverter.floatToByteArray((Float) object, bytes, offset, "float value");
			break;
		case ODBType.DOUBLE_ID:
		case ODBType.NATIVE_DOUBLE_ID:
			objectSize += byteArrayConverter.doubleToByteArray((Double) object, bytes, offset, "double value");
			break;
		case ODBType.INTEGER_ID:
		case ODBType.NATIVE_INT_ID:
			objectSize += byteArrayConverter.intToByteArray((Integer) object, bytes, offset, "int value");
			break;
		case ODBType.LONG_ID:
		case ODBType.NATIVE_LONG_ID:
			objectSize += byteArrayConverter.longToByteArray((Long) object, bytes, offset, "long value");
			break;
		case ODBType.SHORT_ID:
		case ODBType.NATIVE_SHORT_ID:
			objectSize += byteArrayConverter.shortToByteArray((Short) object, bytes, offset, "short value");
			break;
		case ODBType.BIG_DECIMAL_ID:
			objectSize += byteArrayConverter.bigDecimalToByteArray((BigDecimal) object, bytes, offset, "big decimal value");
			break;
		case ODBType.BIG_INTEGER_ID:
			objectSize += byteArrayConverter.bigIntegerToByteArray((BigInteger) object, bytes, offset, "big integer value");
			break;
		case ODBType.DATE_ID:
		case ODBType.DATE_SQL_ID:
		case ODBType.DATE_TIMESTAMP_ID:
			objectSize += byteArrayConverter.dateToByteArray((java.util.Date) object, bytes, offset, "date value");
			break;
		case ODBType.DATE_CALENDAR_ID:
		case ODBType.DATE_GREGORIAN_CALENDAR_ID:
			Calendar c = (Calendar) object;
			objectSize += byteArrayConverter.dateToByteArray(c.getTime(), bytes, offset, "date value");
			break;
		case ODBType.STRING_ID:
			objectSize += byteArrayConverter.stringToByteArray((String) object, true, bytes, offset, "string value");
			break;
		case ODBType.OBJECT_OID_ID:
		case ODBType.OBJECT_OID_IMPL_UUID_ID:
		case ODBType.OBJECT_OID_IMPL_SEQ_ID:
			objectSize += byteArrayConverter.objectOidToByteArray((ObjectOid) object, bytes, offset, "oid value");
			break;
		case ODBType.CLASS_OID_ID:
		case ODBType.CLASS_OID_IMPL_UUID_ID:
		case ODBType.CLASS_OID_IMPL_SEQ_ID:
			objectSize += byteArrayConverter.classOidToByteArray((ClassOid) object, bytes, offset, "oid value");
			break;
		default:
			// FIXME replace RuntimeException by a
			throw new RuntimeException("native type with odb type id " + odbTypeId + " (" + ODBType.getNameFromId(odbTypeId)
					+ ") for attribute ? is not suported");
		}
		return new AttributeIdentification(originalPosition, objectSize);
	}

	/**
	 * Actually write the object data to the database file
	 * 
	 * @param noi
	 *            The object meta info: The object info to be written
	 * @param bytes
	 *            The array where to write
	 * @param offset
	 *            The poition fro which data must be written
	 * @return the size of the object
	 */

	protected AttributeIdentification nativeObjectInfoToBytes(NativeObjectInfo noi, Bytes bytes, int offset, ConversionContext context) {

		if (debug) {
			DLogger.debug("  Start writing native object : Type=" + ODBType.getNameFromId(noi.getOdbTypeId()) + " | Value=" + noi.toString()
					+ " | at " + (offset + bytes.getOffset()));
		}

		try{
			if (noi.isNull()) {
				int objectSize = nativeObjectHeaderToBytes(noi.getOdbTypeId(), true, BlockTypes.BLOCK_TYPE_NATIVE_OBJECT, bytes, offset);
				// The header already has the null info, no more need to write
				return new AttributeIdentification(offset, objectSize);
			}

			if (noi.isAtomicNativeObject()) {
				return atomicNativeObjectToBytes((AtomicNativeObjectInfo) noi, bytes, offset);
			}

			if (noi.isCollectionObject()) {
				return collectionToBytes((CollectionObjectInfo) noi, bytes, offset, context);
			}
			if (noi.isMapObject()) {
				return mapToBytes((MapObjectInfo) noi, bytes, offset, context);
			}
			if (noi.isArrayObject()) {
				return arrayToBytes((ArrayObjectInfo) noi, bytes, offset, context);
			}

			if (noi.isEnumObject()) {
				return enumNativeObjectToBytes((EnumNativeObjectInfo) noi, bytes, offset);
			}
			throw new NeoDatisRuntimeException(NeoDatisError.NATIVE_TYPE_NOT_SUPPORTED.addParameter(noi.getOdbTypeId()));
		}finally{
			if (debug) {
				DLogger.debug("  End writing native object : Type=" + ODBType.getNameFromId(noi.getOdbTypeId()) + " | Value=" + noi.toString()
						+ " | at " + (offset+bytes.getOffset()));
			}
		}
	}

	/**
	 * This method is used to store the object : natibe or non native and return
	 * a number : - The position of the object if it is a native object - The
	 * oid (as a negative number) if it is a non native object
	 * 
	 * @param aoi
	 * @return
	 * @throws Exception
	 */
	protected AttributeIdentification internalToBytesWrapper(AbstractObjectInfo aoi, Bytes bytes, int offset, ConversionContext context) {
		if (aoi.isNative()) {
			return nativeObjectInfoToBytes((NativeObjectInfo) aoi, bytes, offset, context);
		}

		if (aoi.isNonNativeObject() && aoi.isNull()) {
			return new AttributeIdentification(session.getOidGenerator().getNullObjectOid());
		}
		if (aoi.isNonNativeObject() && !aoi.isObjectReference()) {
			internalMetaToBytes((NonNativeObjectInfo) aoi, context);
			ObjectOid oid = context.getLastOid();
			return new AttributeIdentification(oid);
		}

		// Object references are references to object already stored.
		// But in the case of map, the reference can appear before the real
		// object (as order may change)
		// If objectReference.getOid() is null, it is the case. In this case,
		// We take the object being referenced and stores it directly.
		ObjectReference objectReference = (ObjectReference) aoi;
		if (objectReference.getOid() == null) {
			internalMetaToBytes((NonNativeObjectInfo) aoi, context);
			ObjectOid oid = context.getLastOid();
			return new AttributeIdentification(oid);
		}

		return new AttributeIdentification(objectReference.getOid());
	}

	/**
	 * 
	 * <pre>
	 *                          Write a collection to the database
	 *                          
	 *                          This is done by writing the number of elements and then the position of all elements.
	 *                          
	 *                          Example : a list with two string element : 'ola' and 'chico'
	 *                          
	 *                          write 2 (as an int) : the number of elements
	 *                          write two times 0 (as long) to reserve the space for the elements positions
	 *                          
	 *                          then write the string 'ola', and keeps its position in the 'positions' array of long 
	 *                          then write the string 'chico' and keeps its position in the 'positions' array of long
	 *                          
	 *                          Then write back all the positions (in this case , 2 positions) after the size of the collection
	 *                          &lt;pre&gt;
	 *                          &#064;param coi
	 *                          &#064;param writeInTransaction
	 * &#064;
	 * 
	 */
	protected AttributeIdentification collectionToBytes(CollectionObjectInfo coi, Bytes bytes, int offset, ConversionContext context) {

		if (debug) {
			DLogger.debug("  <start writing collection at " + offset + ">");
		}

		int objectSize = nativeObjectHeaderToBytes(coi.getOdbTypeId(), coi.isNull(), BlockTypes.BLOCK_TYPE_COLLECTION_OBJECT, bytes, offset);

		if (coi.isNull()) {
			return new AttributeIdentification(offset, objectSize);
		}
		Collection<AbstractObjectInfo> collection = coi.getCollection();
		int collectionSize = collection.size();
		Iterator<AbstractObjectInfo> iterator = collection.iterator();

		// write the real type of the collection
		objectSize += byteArrayConverter.stringToByteArray(coi.getRealCollectionClassName(), false, bytes, offset + objectSize,
				"real collection class");

		// write the size of the collection
		objectSize += byteArrayConverter.intToByteArray(collectionSize, bytes, offset + objectSize, "collection size");
		// build a n array to store all element identifications
		AttributeIdentification[] attributeIdentifications = new AttributeIdentification[collectionSize];
		

		// Gets the current position, to know later where to put the header position info
		int headerPosition = objectSize + offset;

		// write a fake header position
		objectSize += byteArrayConverter.intToByteArray(0, bytes, headerPosition, "header position");

		
		int currentElement = 0;
		AbstractObjectInfo element = null;
		while (iterator.hasNext()) {
			element = iterator.next();
			AttributeIdentification ai = internalToBytesWrapper(element, bytes, offset+objectSize, context);
			attributeIdentifications[currentElement] = ai;
			currentElement++;
			// for non native object in the collection, we don't write the oid
			// here as it is already written in the header
			// So we only add size for native object
			if (ai.isNative) {
				objectSize += ai.size;
			}
		}

		// write the header position (without increasing the objectSize)
		byteArrayConverter.intToByteArray(offset+objectSize, bytes, headerPosition, "header position");
		
		if (debug) {
			DLogger.debug("     <start writing header collection at" + (offset+objectSize)+ " >");
		}

		// now that all objects have been stored, set their identification in
		// the header
		int headerRealSize = headerToBytes(attributeIdentifications, bytes, objectSize+offset, false);
		if (debug) {
			DLogger.debug("     <end writing header collection at" + (offset+objectSize)+ " >");
		}
		
		if (debug) {
			DLogger.debug("  <end writing collection at " + offset + " size=" + (objectSize+headerRealSize) + ">");
		}

		int totalObjectSize = objectSize+headerRealSize;
		return new AttributeIdentification(offset, totalObjectSize);
	}

	/**
	 * <pre>
	 *                          Write an array to the database
	 *                          
	 *                          This is done by writing :
	 *                          - the array type : array
	 *                          - the array element type (String if it os a String [])
	 *                          - the position of the non native type, if element are non java / C# native
	 *                          - the number of element s and then the position of all elements.
	 *                          
	 *                          Example : an array with two string element : 'ola' and 'chico'
	 *                          write 22 : array
	 *                          write  20 : array of STRING
	 *                          write 0 : it is a java native object
	 *                          write 2 (as an int) : the number of elements
	 *                          write two times 0 (as long) to reserve the space for the elements positions
	 *                          
	 *                          then write the string 'ola', and keeps its position in the 'positions' array of long 
	 *                          then write the string 'chico' and keeps its position in the 'positions' array of long
	 *                          
	 *                          Then write back all the positions (in this case , 2 positions) after the size of the array
	 *                          
	 *                          
	 *                          Example : an array with two User element : user1 and user2
	 *                          write 22 : array
	 *                          write  23 : array of NON NATIVE Objects
	 *                          write 251 : if 250 is the position of the user class info in database
	 *                          write 2 (as an int) : the number of elements
	 *                          write two times 0 (as long) to reserve the space for the elements positions
	 *                          
	 *                          then write the user user1, and keeps its position in the 'positions' array of long 
	 *                          then write the user user2 and keeps its position in the 'positions' array of long
	 *                          &lt;pre&gt;
	 *                          &#064;param object
	 *                          &#064;param odbType
	 *                          &#064;param position
	 *                          &#064;param writeInTransaction
	 * &#064;
	 * 
	 */
	protected AttributeIdentification arrayToBytes(ArrayObjectInfo aoi, Bytes bytes, int offset, ConversionContext context) {

		if (debug) {
			DLogger.debug("<start writing array at " + (offset+bytes.getOffset()) + ">");
		}

		int objectSize = nativeObjectHeaderToBytes(aoi.getOdbTypeId(), aoi.isNull(), BlockTypes.BLOCK_TYPE_ARRAY_OBJECT, bytes, offset);

		if (aoi.isNull()) {
			return new AttributeIdentification(offset, objectSize);
		}

		AbstractObjectInfo[] array = (AbstractObjectInfo[]) aoi.getArray();
		int arraySize = array.length;

		// write the real type of the array
		objectSize += byteArrayConverter.stringToByteArray(aoi.getRealArrayComponentClassName(), false, bytes, offset + objectSize,
				"real array type");

		// write the size of the array
		objectSize += byteArrayConverter.intToByteArray(arraySize, bytes, offset + objectSize, "array size");
		// build a n array to store all element identifications
		AttributeIdentification[] attributeIdentifications = new AttributeIdentification[arraySize];


		// Gets the current position, to know later where to put the header
		int headerPosition = objectSize + offset;

		// write a fake header position
		objectSize += byteArrayConverter.intToByteArray(0, bytes, headerPosition, "header position");

		AbstractObjectInfo element = null;
		for (int i = 0; i < arraySize; i++) {
			element = array[i];

			if (element == null) {
				throw new NeoDatisRuntimeException(NeoDatisError.NOT_YET_IMPLEMENTED);
			}
			AttributeIdentification ai = internalToBytesWrapper(element, bytes , offset+objectSize, context);
			attributeIdentifications[i] = ai;
			// only compute the size for native objects. For non native objects,
			// the oid is written in the header
			if (ai.isNative) {
				objectSize += ai.size;
			}
		}
		// write the header position (without increasing the objectSize)
		byteArrayConverter.intToByteArray(offset+objectSize, bytes, headerPosition, "header position");
		
		if (debug) {
			DLogger.debug("     <start writing header array at" + (offset+objectSize)+ " >");
		}

		// now that all objects have been stored, set their identification in
		// the header
		int headerRealSize = headerToBytes(attributeIdentifications, bytes, objectSize+offset, false);
		if (debug) {
			DLogger.debug("     <end writing header array at" + (offset+objectSize)+ " >");
		}
		
		if (debug) {
			DLogger.debug("  <end writing array at " + offset + " size=" + (objectSize+headerRealSize) + ">");
		}

		int totalObjectSize = objectSize+headerRealSize;
		return new AttributeIdentification(offset, totalObjectSize);


	}

	/**
	 * <pre>
	 *                          Write a map to the database
	 *                          
	 *                          This is done by writing the number of element s and then the key and value pair of all elements.
	 *                          
	 *                          Example : a map with two string element : '1/olivier' and '2/chico'
	 *                          
	 *                          write 2 (as an int) : the number of elements
	 *                          write 4 times 0 (as long) to reserve the space for the elements positions
	 *                          
	 *                          then write the object '1' and 'olivier', and keeps the two posiitons in the 'positions' array of long 
	 *                          then write the object '2' and the string chico' and keep the two position in the 'positions' array of long
	 *                          
	 *                          Then write back all the positions (in this case , 4 positions) after the size of the map
	 *                          
	 *                          &#064;param object
	 *                          &#064;param writeInTransaction To specify if these writes must be done in or out of a transaction
	 * &#064;
	 * 
	 */
	protected AttributeIdentification mapToBytes(MapObjectInfo moi, Bytes bytes, int offset, ConversionContext context) {

		int objectSize = nativeObjectHeaderToBytes(moi.getOdbTypeId(), moi.isNull(), BlockTypes.BLOCK_TYPE_MAP_OBJECT, bytes, offset);

		if (moi.isNull()) {
			return new AttributeIdentification(offset, objectSize);
		}

		Map<AbstractObjectInfo, AbstractObjectInfo> map = moi.getMap();
		int mapSize = map.size();
		Iterator<AbstractObjectInfo> keys = map.keySet().iterator();

		// write the map class
		objectSize += byteArrayConverter.stringToByteArray(moi.getRealMapClassName(), false, bytes, offset + objectSize, "real map class");
		// write the size of the map
		objectSize += byteArrayConverter.intToByteArray(mapSize, bytes, offset + objectSize, "map size");
		// build a n array to store all element identifications
		AttributeIdentification[] attributeIdentifications = new AttributeIdentification[mapSize * 2];

		// get the number of non native objects
		int nbNonNativeObjects = moi.getNonNativeObjects().size();
		int nbNativeObjects = mapSize * 2 - nbNonNativeObjects;


		// Gets the current position, to know later where to put the header
		int headerPosition = objectSize + offset;
		
		// real position will be written at the end of the method
		objectSize+= byteArrayConverter.intToByteArray(0, bytes, offset + objectSize, "header pos");
		
		int currentElement = 0;
		AttributeIdentification ai = null;
		while (keys.hasNext()) {
			AbstractObjectInfo key = keys.next();
			AbstractObjectInfo value = map.get(key);

			ODBType keyType = ODBType.getFromClass(key.getClass());
			ODBType valueType = ODBType.getFromClass(value.getClass());

			ai = internalToBytesWrapper(key, bytes, offset + objectSize, context);
			attributeIdentifications[currentElement++] = ai;
			// Only add size for native objects as non native are not written
			// here
			if (ai.isNative) {
				objectSize += ai.size;
			}

			ai = internalToBytesWrapper(value, bytes,offset + objectSize, context);
			attributeIdentifications[currentElement++] = ai;
			if (ai.isNative) {
				objectSize += ai.size;
			}
		}
		// now that all objects have been stored, set their identification in
		// the header
		// write header position
		byteArrayConverter.intToByteArray(offset + objectSize, bytes, headerPosition, "header pos");
		
		int headerRealSize = headerToBytes(attributeIdentifications, bytes, offset+objectSize, false);
		
		objectSize += headerRealSize;

		// Write object size
		objectSize+= byteArrayConverter.intToByteArray(objectSize, bytes, offset + objectSize, "object size");
		// Write Crc
		int crc = computeCrc(bytes);
		objectSize+=byteArrayConverter.intToByteArray(crc,bytes,offset+ objectSize, "crc");

		return new AttributeIdentification(offset, objectSize);

	}

	protected AttributeIdentification enumNativeObjectToBytes(EnumNativeObjectInfo anoi, Bytes bytes, int offset) {
		int odbTypeId = anoi.getOdbTypeId();

		int objectSize = nativeObjectHeaderToBytes(odbTypeId, anoi.isNull(), BlockTypes.BLOCK_TYPE_NATIVE_OBJECT, bytes, offset);

		// Writes the Enum Class id
		objectSize += byteArrayConverter.classOidToByteArray(anoi.getEnumClassInfo().getOid(), bytes, offset + objectSize, "enum class id");
		// Write the Enum String value
		objectSize += byteArrayConverter.stringToByteArray(anoi.getObject().toString(), true, bytes, offset + objectSize, "enum value");
		return new AttributeIdentification(offset, objectSize);

	}

	public IOdbList<OidAndBytes> classInfoToBytes(ClassInfo classInfo) {

		if (debug) {
			DLogger.debug(String.format("<ci start writing class info with oid %s : %s>", classInfo.getOid(), classInfo.toString()));
		}

		Bytes bytes = BytesFactory.getBytes();
		int objectSize = 0;
		// check sum
		objectSize += byteArrayConverter.intToByteArray(0, bytes, objectSize, "ci check sum");
		// Real value of block size is only known at the end of the writing
		objectSize += byteArrayConverter.intToByteArray(0, bytes, objectSize, "ci object size");
		objectSize += byteArrayConverter.byteToByteArray(BlockTypes.BLOCK_TYPE_CLASS_HEADER, bytes, objectSize, "ci object type");
		objectSize += byteArrayConverter.byteToByteArray(classInfo.getClassCategory(), bytes, objectSize, "ci category");
		objectSize += byteArrayConverter.classOidToByteArray(classInfo.getOid(), bytes, objectSize, "ci id");
		objectSize += byteArrayConverter.classOidToByteArray(classInfo.getSuperClassOid(), bytes, objectSize, "ci super class id");
		objectSize += byteArrayConverter.stringToByteArray(classInfo.getFullClassName(), false, bytes, objectSize, "ci name");
		objectSize += byteArrayConverter.intToByteArray(classInfo.getMaxAttributeId(), bytes, objectSize, "max attribute id");
		objectSize += byteArrayConverter.intToByteArray(classInfo.getAttributes().size(), bytes, objectSize, "ci nb attributes");

		ClassAttributeInfo cai = null;
		for (int i = 0; i < classInfo.getAttributes().size(); i++) {
			cai = classInfo.getAttributes().get(i);
			objectSize += classAttributeInfoToBytes(cai, bytes, objectSize);
		}

		byteArrayConverter.intToByteArray(objectSize, bytes, POS_CI_OBJECT_SIZE, "ci object size");

		OidAndBytes oidAndBytes = new OidAndBytes(classInfo.getOid(), bytes);
		IOdbList<OidAndBytes> l = new OdbArrayList<OidAndBytes>();
		l.add(oidAndBytes);
		
		if (debug) {
			DLogger.debug(String.format("<ci end writing class info with oid %s >", classInfo.getOid()));
		}

		return l;
	}

	/**
	 * Writes a class attribute info, an attribute of a class
	 * 
	 * @param cai
	 * @param bytes
	 *            The bytes where to write
	 * @param the
	 *            current position where to write
	 * @return The position after write
	 */
	private int classAttributeInfoToBytes(ClassAttributeInfo cai, Bytes bytes, int position) {
		int originalPosition = position;
		position += byteArrayConverter.intToByteArray(cai.getId(), bytes, position, "attribute id");
		position += byteArrayConverter.booleanToByteArray(cai.isNative(), bytes, position, "is native?");

		if (cai.isNative()) {
			position += byteArrayConverter.intToByteArray(cai.getAttributeType().getId(), bytes, position, "att odb type id");
			if (cai.getAttributeType().isArray()) {
				ODBType subType = cai.getAttributeType().getSubType();
				position += byteArrayConverter.intToByteArray(subType.getId(), bytes, position, "att array sub type");
				// when the attribute is not native, then write its class info
				// position
				if (subType.isNonNative()) {
					// get the class info of the subtype, TODO
					ClassInfo ci = session.getClassInfo(subType.getName());
					position += byteArrayConverter.classOidToByteArray(ci.getOid(), bytes, position, "class info id of array subtype");
				}
			} else if (cai.getAttributeType().isEnum()) {
				// For enum, we write the class info id of the enum class
				ClassInfo enumCi = session.getClassInfo(cai.getClassName());
				position += byteArrayConverter.classOidToByteArray(enumCi.getOid(), bytes, position, "class info id of array subtype");
			}
		} else {
			position += byteArrayConverter.classOidToByteArray(cai.getAttributeClassOid(), bytes, position, "attribute class info id");
		}
		position += byteArrayConverter.stringToByteArray(cai.getName(), false, bytes, position, "attribute name");

		// we need to return the size of what was written
		return (position - originalPosition);
	}

	public void writeDatabaseHeader(DatabaseInfo di) {
		
	}

}
